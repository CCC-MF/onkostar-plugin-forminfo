/*
 * MIT License
 *
 * Copyright (c) 2023 Comprehensive Cancer Center Mainfranken
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.ukw.ccc.onkostar.forminfo;

import de.itc.onkostar.api.Disease;
import de.itc.onkostar.api.IOnkostarApi;
import de.itc.onkostar.api.Procedure;
import de.itc.onkostar.api.analysis.AnalyzerRequirement;
import de.itc.onkostar.api.analysis.IProcedureAnalyzer;
import de.itc.onkostar.api.analysis.OnkostarPluginType;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class FormInfoPlugin implements IProcedureAnalyzer {

    private final IOnkostarApi onkostarApi;

    private final JdbcTemplate jdbcTemplate;

    FormInfoPlugin(final IOnkostarApi onkostarApi, final DataSource dataSource) {
        this.onkostarApi = onkostarApi;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public OnkostarPluginType getType() {
        return OnkostarPluginType.BACKEND_SERVICE;
    }

    @Override
    public String getVersion() {
        return "0.1.0";
    }

    @Override
    public String getName() {
        return "CCC-MF FormInfo Plugin";
    }

    @Override
    public String getDescription() {
        return "Plugin to provide form and procedure informations";
    }

    @Override
    public boolean isSynchronous() {
        return false;
    }

    @Override
    public AnalyzerRequirement getRequirement() {
        return AnalyzerRequirement.PROCEDURE;
    }

    @Override
    public boolean isRelevantForDeletedProcedure() {
        return false;
    }

    @Override
    public boolean isRelevantForAnalyzer(Procedure procedure, Disease disease) {
        return false;
    }

    @Override
    public void analyze(Procedure procedure, Disease disease) {
        // Nothing to do - should never be called
    }

    /**
     * This method returns content of given form
     * @param data Data map containing procedure ID of requested form.
     * @return Map containing content data
     */
    public Map<String, Object> getContent(Map<String, Object> data) {
        var id = Integer.parseInt(data.get("id").toString());
        var procedure = onkostarApi.getProcedure(id);

        var result = new HashMap<String, Object>();

        var formName = procedure.getFormName();

        procedure.getAllValues().forEach((name, value) -> {
            if (null == value.getValue()) {
                return;
            }

            var description = getDataFormEntryDescriptions(formName, value.getName());

            if (null != value.getDate()) {
                result.put(name, new Result(name, description, new SimpleDateFormat("dd.MM.yyyy").format(value.getDate())));
                return;
            }

            if (null != value.getPropertyCatalogueVersion()) {
                var catalogueEntry = getPropertyCatalogueEntry(value.getPropertyCatalogueVersion(), value.getString());
                if (null != catalogueEntry) {
                    result.put(name, new Result(name, description, catalogueEntry));
                    return;
                }
            }

            result.put(name, new Result(name, description, value.getValue()));

        });

        return result;
    }

    private String getPropertyCatalogueEntry(String propertyCatalogueVersionEntry, String code) {
        var sql = "SELECT shortdesc FROM property_catalogue_version_entry WHERE property_version_id = ? AND code = ? LIMIT 1";
        var shortdesc = jdbcTemplate.query(sql, new Object[]{propertyCatalogueVersionEntry, code}, (resultSet, i) -> resultSet.getString("shortdesc"));
        if (shortdesc.size() > 0) {
            return shortdesc.get(0);
        }
        return null;
    }

    private String getDataFormEntryDescriptions(String formName, String name) {
        var sql = "SELECT dfe.name, dfe.type, dfe.description FROM data_form\n" +
                "    JOIN data_form_entry dfe ON data_form.id = dfe.data_form_id\n" +
                "    WHERE data_form.name = ? AND dfe.name = ? LIMIT 1";

        var description = jdbcTemplate.query(sql, new Object[]{formName, name}, (resultSet, i) -> resultSet.getString("description"));
        if (description.size() > 0) {
            return description.get(0);
        }
        return null;
    }

    private static class Result {
        public String field;
        public String description;
        public Object value;

        public Result(String field, String description, Object value) {
            this.field = field;
            this.description = description;
            this.value = value;
        }
    }
}
