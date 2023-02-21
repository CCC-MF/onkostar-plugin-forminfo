package de.ukw.ccc.onkostar.forminfo;

enum Type {
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
