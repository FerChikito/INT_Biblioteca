package org.example.int_biblioteca;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PrestamoListado {
    private final int idPrestamo;
    private final String correoUsuario;
    private final String titulo;
    private final String isbn;
    private final String estado;            // 'A'/'D'
    private final LocalDateTime fechaPrestamo;
    private final LocalDateTime fechaDevolucion; // puede ser null
    private final String folio;             // NUEVO
    private final LocalDate fechaLimite;    // NUEVO

    public PrestamoListado(
            int idPrestamo,
            String correoUsuario,
            String titulo,
            String isbn,
            String estado,
            LocalDateTime fechaPrestamo,
            LocalDateTime fechaDevolucion,
            String folio,
            LocalDate fechaLimite
    ) {
        this.idPrestamo = idPrestamo;
        this.correoUsuario = correoUsuario;
        this.titulo = titulo;
        this.isbn = isbn;
        this.estado = estado;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.folio = folio;
        this.fechaLimite = fechaLimite;
    }

    public int getIdPrestamo() { return idPrestamo; }
    public String getCorreoUsuario() { return correoUsuario; }
    public String getTitulo() { return titulo; }
    public String getIsbn() { return isbn; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaPrestamo() { return fechaPrestamo; }
    public LocalDateTime getFechaDevolucion() { return fechaDevolucion; }
    public String getFolio() { return folio; }
    public LocalDate getFechaLimite() { return fechaLimite; }

}
