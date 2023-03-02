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

package de.ukw.ccc.onkostar.forminfo.services;

import de.itc.onkostar.api.IOnkostarApi;
import de.itc.onkostar.api.Procedure;
import de.ukw.ccc.onkostar.forminfo.FormInfoException;
import de.ukw.ccc.onkostar.forminfo.Result;
import de.ukw.ccc.onkostar.forminfo.Type;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default implementation of FormInfoService
 *
 * @author Paul-Christian Volkmer
 * @since 0.1.0
 */
@Service
class DefaultFormInfoService implements FormInfoService {

    private final IOnkostarApi onkostarApi;

    private final JdbcTemplate jdbcTemplate;

    DefaultFormInfoService(final IOnkostarApi onkostarApi, final DataSource dataSource) {
        this.onkostarApi = onkostarApi;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<Result> getFormInfo(int procedureId) {
        var procedure = onkostarApi.getProcedure(procedureId);

        if (null == procedure) {
            throw new FormInfoException(String.format("No such procedure with ID '%d'", procedureId));
        }

        return getDataFormEntry(procedure, null)
                .stream()
                .flatMap(element -> flattenEntry(element).stream())
                .map(getResultFunction(procedure)).collect(Collectors.toList());
    }

    @Override
    public int getMainFormProcedureId(int procedureId) {
        var sql = "SELECT hauptprozedur_id FROM prozedur WHERE id = ?";
        try {
            return jdbcTemplate
                    .queryForObject(sql, (resultSet, i) ->
                                    resultSet.getInt("hauptprozedur_id")
                            , procedureId);
        } catch (Exception e) {
            throw new FormInfoException(String.format("No main form found for subform with ID '%d'", procedureId));
        }
    }

    private Function<Entry, Result> getResultFunction(Procedure procedure) {
        return entry -> {
            var value = procedure.getValue(entry.name);

            if (null != value.getDate()) {
                return new Result(
                        entry.name,
                        entry.description,
                        new SimpleDateFormat("dd.MM.yyyy").format(value.getDate()),
                        entry.type
                );
            }

            if (null != value.getPropertyCatalogueVersion()) {
                var catalogueEntry = getPropertyCatalogueEntry(value.getPropertyCatalogueVersion(), value.getString());
                if (null != catalogueEntry) {
                    return new Result(entry.name, entry.description, catalogueEntry, entry.type);
                }
            }

            return new Result(entry.name, entry.description, value.getValue(), entry.type);
        };
    }

    private String getPropertyCatalogueEntry(String propertyCatalogueVersionEntry, String code) {
        var sql = "SELECT shortdesc FROM property_catalogue_version_entry WHERE property_version_id = ? AND code = ? LIMIT 1";
        var shortdesc = jdbcTemplate.query(sql, new Object[]{propertyCatalogueVersionEntry, code}, (resultSet, i) -> resultSet.getString("shortdesc"));
        if (shortdesc.size() > 0) {
            return shortdesc.get(0);
        }
        return null;
    }

    private List<Entry> getDataFormEntry(Procedure procedure, Integer parentId) {
        var formName = procedure.getFormName();

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
                    var children = getDataFormEntry(procedure, id);
                    return new Entry(
                            resultSet.getString("name"),
                            resultSet.getString("description"),
                            Type.from(resultSet.getString("type")),
                            children
                    );
                }
        );
    }

    static List<Entry> flattenEntry(Entry element) {
        var result = new ArrayList<Entry>();
        result.add(element);
        result.addAll(element.children
                .stream().flatMap(e -> flattenEntry(e).stream())
                .collect(Collectors.toList()));
        return result;
    }

    static class Entry {
        public final String name;
        public final String description;
        public final Type type;
        public final List<Entry> children = new ArrayList<>();

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

}
