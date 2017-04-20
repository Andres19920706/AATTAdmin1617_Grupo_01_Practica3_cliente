/**
 *
 */
package aatt.practica03.model;

/**
 * Clase que contiene la estructura de un Usuario.
 * @author Juan Carlos Cuevas Martínez
 * @author Andres Ruiz Peñuela
 * @author Luis Jesús Montes Pérez
 * @version 3.0.0
 */
public class Usuario {
    /* Atributos. */
    private String nombre;
    private String apellido1;
    private String apellido2;
    private String nif;
    
    /* Constructor por defecto. */
    public Usuario ()
    {
        this.nombre="";
        this.apellido1="";
        this.apellido2="";
        this.nif="";
    }
    /**
     * Constructor con parametros.
     * @param n de tipo String
     * @param a1 de tipo String
     * @param a2 de tipo String
     * @param ni de tipo String
     */
     
    public Usuario(String n,String a1,String a2,String ni){
        nombre=n;
        apellido1=a1;
        apellido2=a2;
        nif=ni;
    }
    /**
     * Muestra los datos del usuario.
     * @return objeto de tipo String
     */
    @Override
    public String toString(){
        return "-Nombre, Apellidos: "+nombre+", "+apellido1+" "+apellido2+".\n -DNI: "+nif+".";
    }
    
    /* Métodos de acceso. */
    //Metodos Get
    /**
     * Metodo para obtener el nombre 
     * @return nombre de tipo String
     */
    public String getNombre() {
        return nombre;
    }
     /**
     * Metodo para obtener el primer apellido  
     * @return apellido1 de tipo String
     */
    public String getApellido1() {
        return apellido1;
    }
     /**
     * Metodo para obtener el segundo apellido 
     * @return apellido2 de tipo String
     */
    public String getApellido2() {
        return apellido2;
    }
     /**
     * Metodo para obtener el nif
     * @return nif de tipo String
     */
    public String getNif() {
        return nif;
    }
    //Métodos set.
     /**
     * Metodo para insertar el nombre 
     * @param nombre de tipo String
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
     /**
     * Metodo para insertar el primer apellido 
     * @param apellido1 de tipo String
     */
    public void setApellido1(String apellido1) {
        this.apellido1 = apellido1;
    }
     /**
     * Metodo para insertar el segundo apellido 
     * @param apellido2 de tipo String
     */
    public void setApellido2(String apellido2) {
        this.apellido2 = apellido2;
    }
     /**
     * Metodo para insertar el el nif
     * @param nif de tipo String
     */
    public void setNif(String nif) {
        this.nif = nif;
    }

}