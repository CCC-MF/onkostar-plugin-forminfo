package de.ukw.ccc.onkostar.forminfo;

import de.itc.onkostar.api.IOnkostarApi;
import de.itc.onkostar.api.Procedure;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
class FormInfoService {

    private final IOnkostarApi onkostarApi;

    private final JdbcTemplate jdbcTemplate;

    FormInfoService(final IOnkostarApi onkostarApi, final DataSource dataSource) {
        this.onkostarApi = onkostarApi;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<Result> getFormInfo(int procedureId) {
        var procedure = onkostarApi.getProcedure(procedureId);

        return getDataFormEntry(procedure, null)
                .stream()
                .flatMap(element -> flattenEntry(element).stream())
                .map(getResultFunction(procedure)).collect(Collectors.toList());
    }

    private Function<Entry, Result> getResultFunction(Procedure procedure) {
        return elem -> {
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

}
