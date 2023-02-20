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
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
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
     *
     * @param data Data map containing procedure ID of requested form.
     * @return List containing content data
     */
    public List<Result> getContent(Map<String, Object> data) {
        var id = 0;
        try {
            id = Integer.parseInt(data.get("id").toString());
        } catch (Exception e) {
            throw new RuntimeException(String.format("Invalid procedure id: {}", data.get("id")));
        }
        var procedure = onkostarApi.getProcedure(id);
        var formName = procedure.getFormName();

        return getDataFormEntry(formName, null)
                .stream()
                .flatMap(element -> flattenEntry(element).stream())
                .map(elem -> {
                    var value = procedure.getValue(elem.name);

                    if (null != value.getDate()) {
                        return new Result(
                                elem.name,
                                elem.description,
                                new SimpleDateFormat("dd.MM.yyyy").format(value.getDate()),
                                elem.type
                        );
                    }

                    if (null != value.getPropertyCatalogueVersion()) {
                        var catalogueEntry = getPropertyCatalogueEntry(value.getPropertyCatalogueVersion(), value.getString());
                        if (null != catalogueEntry) {
                            return new Result(elem.name, elem.description, catalogueEntry, elem.type);
                        }
                    }

                    return new Result(elem.name, elem.description, value.getValue(), elem.type);
                }).collect(Collectors.toList());
    }

    private String getPropertyCatalogueEntry(String propertyCatalogueVersionEntry, String code) {
        var sql = "SELECT shortdesc FROM property_catalogue_version_entry WHERE property_version_id = ? AND code = ? LIMIT 1";
        var shortdesc = jdbcTemplate.query(sql, new Object[]{propertyCatalogueVersionEntry, code}, (resultSet, i) -> resultSet.getString("shortdesc"));
        if (shortdesc.size() > 0) {
            return shortdesc.get(0);
        }
        return null;
    }

    private List<Entry> getDataFormEntry(String formName, Integer parentId) {
        var sql = "SELECT dfe.id, dfe.name, dfe.type, dfe.description, dfe.element_parent_id FROM data_form " +
                "    JOIN data_form_entry dfe ON data_form.id = dfe.data_form_id " +
                "    WHERE data_form.name = ? AND dfe.element_parent_id = ? ORDER BY dfe.position";

        var variables = new Object[]{formName, parentId};

        if (parentId == null) {
            sql = "SELECT dfe.id, dfe.name, dfe.description, dfe.type, dfe.element_parent_id FROM data_form " +
                    "    JOIN data_form_entry dfe ON data_form.id = dfe.data_form_id " +
                    "    WHERE data_form.name = ? AND dfe.element_parent_id IS NULL ORDER BY dfe.position";

            variables = new Object[]{formName};
        }

        return jdbcTemplate.query(
                sql,
                variables,
                (resultSet, i) -> {
                    var id = resultSet.getInt("id");
                    var children = getDataFormEntry(formName, id);
                    return new Entry(
                            resultSet.getString("name"),
                            resultSet.getString("description"),
                            Type.from(resultSet.getString("type")),
                            children
                    );
                }
        );
    }

    private List<Entry> flattenEntry(Entry element) {
        var result = new ArrayList<Entry>();
        result.add(element);
        result.addAll(element.children
                .stream().flatMap(e -> flattenEntry(e).stream())
                .collect(Collectors.toList()));
        return result;
    }

    private static class Entry {
        public String name;
        public String description;
        public Type type;
        public List<Entry> children = new ArrayList<>();

        Entry(String name, String description, Type type) {
            this(name, description, type, List.of());
        }

        Entry(String name, String description, Type type, List<Entry> children) {
            this.name = name;
            this.description = description;
            this.type = type;
            this.children.addAll(children);
        }
    }

    private static class Result {
        public String field;
        public String description;
        public Object value;
        public Type type;

        public Result(String field, String description, Object value, Type type) {
            this.field = field;
            this.description = description;
            this.value = value;
            this.type = type;
        }
    }

    private enum Type {
        BUTTON,
        FORM_REFERENCE,
        GROUP,
        SECTION,
        SUBFORM,
        INPUT;

        static Type from(String type) {
            switch (type) {
                case "button":
                    return Type.BUTTON;
                case "formReference":
                    return Type.FORM_REFERENCE;
                case "group":
                    return Type.GROUP;
                case "section":
                    return Type.SECTION;
                case "subform":
                    return Type.SUBFORM;
                default:
                    return Type.INPUT;
            }
        }
    }
}
