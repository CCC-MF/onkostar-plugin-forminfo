package de.ukw.ccc.onkostar.forminfo;

class Result {
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
