package org.example.int_biblioteca;

public class Usuario {
    private String nombre;
    private String correo;             // IDENTIFICADOR ÚNICO
    private String numeroTelefonico;
    private String direccion;
    private String contrasenia;
    private Rol rol;

    public Usuario(String nombre, String correo, String contrasenia, Rol rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.rol = rol;
    }

    public Usuario(String nombre, String correo, String numeroTelefonico,
                   String direccion, String contrasenia, Rol rol) {
        this(nombre, correo, contrasenia, rol);
        this.numeroTelefonico = numeroTelefonico;
        this.direccion = direccion;
    }

    // Getters/Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getNumeroTelefonico() { return numeroTelefonico; }
    public void setNumeroTelefonico(String numeroTelefonico) { this.numeroTelefonico = numeroTelefonico; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getContrasenia() { return contrasenia; }
    public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
}
