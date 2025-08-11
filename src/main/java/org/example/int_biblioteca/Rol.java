package org.example.int_biblioteca;

public enum Rol { SUPER_ADMIN, ADMIN, BIBLIOTECARIO, USUARIO;
    public static Rol fromDb(String s) {
        if (s == null) return USUARIO;
        try { return Rol.valueOf(s.trim().toUpperCase()); }
        catch (Exception e) { return USUARIO; }
    }

}

