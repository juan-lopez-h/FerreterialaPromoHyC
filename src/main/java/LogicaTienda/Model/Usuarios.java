package LogicaTienda.Model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Clase que representa un usuario del sistema
 */
@Getter
@Setter
public class Usuarios implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String idUsuario;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String username;
    private String rol;
    private boolean activo;
    private Date fechaCreacion;
    private Date ultimoAcceso;
    private List<String> permisos;
    private List<Domicilios> domicilios;

    // No-arg constructor
    public Usuarios() {
        this.idUsuario = "";
        this.nombre = "";
        this.apellido = "";
        this.email = "";
        this.telefono = "";
        this.username = "";
        this.rol = "CLIENTE";
        this.activo = true;
        this.fechaCreacion = new Date();
        this.ultimoAcceso = null;
        this.permisos = new ArrayList<>();
        this.domicilios = new ArrayList<>();
    }

    public Usuarios(String idUsuario, String nombre, String apellido, String email, String username, String password) {
        this();
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.username = username;
    }

    /**
     * Agrega un domicilio a la lista de domicilios del usuario
     * @param domicilio Domicilio a agregar
     */
    public void agregarDomicilio(Domicilios domicilio) {
        if (domicilios == null) {
            domicilios = new ArrayList<>();
        }
        domicilios.add(domicilio);
    }

    /**
     * Elimina un domicilio de la lista de domicilios del usuario
     * @param idDomicilio ID del domicilio a eliminar
     */
    public void eliminarDomicilio(String idDomicilio) {
        if (domicilios != null) {
            domicilios.removeIf(d -> d != null && d.getIdDomicilio() != null && d.getIdDomicilio().equals(idDomicilio));
        }
    }

    /**
     * Agrega un permiso al usuario
     * @param permiso Permiso a agregar
     */
    public void agregarPermiso(String permiso) {
        if (permisos == null) {
            permisos = new ArrayList<>();
        }
        if (permiso != null && !permisos.contains(permiso)) {
            permisos.add(permiso);
        }
    }

    /**
     * Verifica si el usuario tiene un permiso específico
     * @param permiso Permiso a verificar
     * @return true si tiene el permiso, false en caso contrario
     */
    public boolean tienePermiso(String permiso) {
        return permisos != null && permisos.contains(permiso);
    }

    /**
     * Actualiza la fecha de último acceso
     */
    public void actualizarUltimoAcceso() {
        this.ultimoAcceso = new Date();
    }

    /**
     * Obtiene el nombre completo del usuario
     * @return Nombre completo
     */
    public String getNombreCompleto() {
        return this.nombre + " " + this.apellido;
    }

    public String toString() {
        return "Usuario: " + this.username + " (" + this.getNombreCompleto() + ") - Rol: " + this.rol;
    }
}