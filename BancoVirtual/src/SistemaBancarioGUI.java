import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

// Enums y clases auxiliares
enum TipoCuenta {
    AHORRO, ESTUDIANTE, MENOR_EDAD, NOMINA, EMPRESA, BIENESTAR, PERSONAL
}

enum CategoriaProducto {
    PERSONAL_BASICA, PREMIUM_INDIVIDUAL, JUVENIL_ESTUDIANTIL,
    MENOR_TUTELADO, CORPORATIVO_EMPRESARIAL, NOMINA_SALARIAL,
    BENEFICIARIO_SOCIAL
}

// Clase TarjetaCredito
class TarjetaCredito {
    private String tipo;
    private double limiteCredito;
    private double saldoUsado;

    public TarjetaCredito(String tipo, double limiteCredito) {
        this.tipo = tipo;
        this.limiteCredito = limiteCredito;
        this.saldoUsado = 0;
    }

    public String getTipo() { return tipo; }
    public double getLimiteCredito() { return limiteCredito; }
    public double getSaldoDisponible() { return limiteCredito - saldoUsado; }

    public boolean usarCredito(double monto) {
        if (monto <= getSaldoDisponible()) {
            saldoUsado += monto;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Tarjeta: " + tipo + " | Límite: $" + limiteCredito + " | Usado: $" + saldoUsado;
    }
}

// Clase SolicitudTarjetaCredito
class SolicitudTarjetaCredito {
    private String nombreCliente;
    private String motivo;
    private String numeroTarjeta;
    private boolean atendida;
    private String tipoDeseado;
    private double montoSolicitado;
    private String ocupacion;

    public SolicitudTarjetaCredito(String nombreCliente, String motivo, String numeroTarjeta,
                                   String tipoDeseado, double montoSolicitado, String ocupacion) {
        this.nombreCliente = nombreCliente;
        this.motivo = motivo;
        this.numeroTarjeta = numeroTarjeta;
        this.tipoDeseado = tipoDeseado;
        this.montoSolicitado = montoSolicitado;
        this.ocupacion = ocupacion;
        this.atendida = false;
    }

    public String getNombreCliente() { return nombreCliente; }
    public String getMotivo() { return motivo; }
    public String getNumeroTarjeta() { return numeroTarjeta; }
    public boolean estaAtendida() { return atendida; }
    public void marcarComoAtendida() { this.atendida = true; }
    public String getTipoDeseado() { return tipoDeseado; }
    public double getMontoSolicitado() { return montoSolicitado; }
    public String getOcupacion() { return ocupacion; }
}

// Clase Operacion
class RegistroTransaccion {
    private String tipoMovimiento;
    private double importeOperacion;
    private LocalDateTime fechaHoraRegistro;
    private String detalleDescriptivo;

    public RegistroTransaccion(String tipo, double importe, String detalle) {
        this.tipoMovimiento = tipo;
        this.importeOperacion = importe;
        this.detalleDescriptivo = detalle;
        this.fechaHoraRegistro = LocalDateTime.now();
    }

    public String obtenerTipoMovimiento() { return tipoMovimiento; }
    public double obtenerImporteOperacion() { return importeOperacion; }
    public String obtenerDetalleDescriptivo() { return detalleDescriptivo; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return String.format("[%s] %s - $%.2f - %s",
                fechaHoraRegistro.format(formatter), tipoMovimiento,
                importeOperacion, detalleDescriptivo);
    }
}

// Clase ProductoInversion
class ProductoInversion {
    private double montoCapitalInicial;
    private double porcentajeRendimiento;
    private int diasPlazoInversion;
    private LocalDateTime fechaCreacionInversion;

    public ProductoInversion(double capital, double tasa, int plazo) {
        this.montoCapitalInicial = capital;
        this.porcentajeRendimiento = tasa;
        this.diasPlazoInversion = plazo;
        this.fechaCreacionInversion = LocalDateTime.now();
    }

    public double calcularRendimientoTotal() {
        return montoCapitalInicial * (1 + (porcentajeRendimiento * diasPlazoInversion / 365.0));
    }

    public double obtenerMontoCapital() { return montoCapitalInicial; }
    public double obtenerPorcentajeRendimiento() { return porcentajeRendimiento; }
    public int obtenerDiasPlazo() { return diasPlazoInversion; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return String.format("Inversión: $%.2f - %.2f%% - %d días (Creada: %s)",
                montoCapitalInicial, porcentajeRendimiento * 100,
                diasPlazoInversion, fechaCreacionInversion.format(formatter));
    }
}

// Clase CuentaBancaria
class CuentaBancaria {
    private String nombreTitularCompleto;
    private String numeroTelefonoContacto;
    private int edadTitular;
    private String codigoIdentificacionFiscal;
    private String direccionDomicilioCompleta;
    private double saldoDisponibleActual;
    private String claveAccesoPersonal;
    private String numeroTarjetaPlastica;
    private int identificadorCuentaUnico;
    private CategoriaProducto tipoProductoBancario;
    private List<RegistroTransaccion> historialMovimientos;
    private List<ProductoInversion> carteraInversiones;
    private TarjetaCredito tarjetaCreditoActiva;
    private boolean cuentaBloqueada = false;

    public CuentaBancaria(String nombre, String telefono, int edad, String curp,
                          String direccion, double saldoInicial, String pin,
                          String tarjeta, int id, CategoriaProducto categoria) {
        this.nombreTitularCompleto = nombre;
        this.numeroTelefonoContacto = telefono;
        this.edadTitular = edad;
        this.codigoIdentificacionFiscal = curp;
        this.direccionDomicilioCompleta = direccion;
        this.saldoDisponibleActual = saldoInicial;
        this.claveAccesoPersonal = pin;
        this.numeroTarjetaPlastica = tarjeta;
        this.identificadorCuentaUnico = id;
        this.tipoProductoBancario = categoria;
        this.historialMovimientos = new ArrayList<>();
        this.carteraInversiones = new ArrayList<>();
    }

    // Métodos de la cuenta
    public boolean estaBloqueada() { return cuentaBloqueada; }
    public void bloquearCuenta() { cuentaBloqueada = true; }
    public void desbloquearCuenta() { cuentaBloqueada = false; }

    public void asignarTarjetaCredito(String tipo, double limite) {
        this.tarjetaCreditoActiva = new TarjetaCredito(tipo, limite);
    }

    public TarjetaCredito obtenerTarjetaCredito() { return tarjetaCreditoActiva; }

    public boolean ejecutarDeposito(double cantidad) {
        if (cantidad <= 0) return false;
        this.saldoDisponibleActual += cantidad;
        return true;
    }

    public boolean procesarRetiro(double cantidad) {
        if (cantidad <= 0 || this.saldoDisponibleActual < cantidad) return false;
        this.saldoDisponibleActual -= cantidad;
        return true;
    }

    public void registrarMovimiento(RegistroTransaccion transaccion) {
        this.historialMovimientos.add(transaccion);
    }

    public void agregarProductoInversion(ProductoInversion inversion) {
        this.carteraInversiones.add(inversion);
    }

    // Getters
    public String obtenerNombreTitular() { return nombreTitularCompleto; }
    public String obtenerNumeroTarjeta() { return numeroTarjetaPlastica; }
    public String obtenerClaveAcceso() { return claveAccesoPersonal; }
    public double consultarSaldoActual() { return saldoDisponibleActual; }
    public CategoriaProducto obtenerTipoProducto() { return tipoProductoBancario; }
    public List<ProductoInversion> obtenerCarteraInversiones() { return carteraInversiones; }
    public int obtenerIdCuenta() { return identificadorCuentaUnico; }
    public List<RegistroTransaccion> obtenerHistorialMovimientos() { return historialMovimientos; }
}

// Clase ColaboradorBancario
class ColaboradorBancario {
    private String nombreCompletoEmpleado;
    private String codigoEmpleadoUnico;
    private String cargoDesempenado;

    public ColaboradorBancario(String nombre, String codigo, String cargo) {
        this.nombreCompletoEmpleado = nombre;
        this.codigoEmpleadoUnico = codigo;
        this.cargoDesempenado = cargo;
    }

    public String obtenerNombreCompleto() { return nombreCompletoEmpleado; }
    public String obtenerCodigoEmpleado() { return codigoEmpleadoUnico; }
    public String obtenerCargoDesempenado() { return cargoDesempenado; }

    @Override
    public String toString() {
        return String.format("%s [%s] - %s", nombreCompletoEmpleado, codigoEmpleadoUnico, cargoDesempenado);
    }
}

// Clase InstitucionFinanciera
class InstitucionFinanciera {
    private String denominacionComercial;
    private List<CuentaBancaria> registroCuentasActivas;
    private List<ColaboradorBancario> plantillaPersonal;
    private List<SolicitudTarjetaCredito> solicitudesTarjetas;

    public InstitucionFinanciera(String nombre) {
        this.denominacionComercial = nombre;
        this.registroCuentasActivas = new ArrayList<>();
        this.plantillaPersonal = new ArrayList<>();
        this.solicitudesTarjetas = new ArrayList<>();
    }

    public void incorporarCuentaNueva(CuentaBancaria cuenta) {
        this.registroCuentasActivas.add(cuenta);
    }

    public void incorporarColaborador(ColaboradorBancario empleado) {
        this.plantillaPersonal.add(empleado);
    }

    public CuentaBancaria localizarCuentaPorTarjeta(String numeroTarjeta) {
        return registroCuentasActivas.stream()
                .filter(cuenta -> cuenta.obtenerNumeroTarjeta().equals(numeroTarjeta))
                .findFirst()
                .orElse(null);
    }

    public ColaboradorBancario localizarEmpleadoPorCodigo(String codigoEmpleado) {
        return plantillaPersonal.stream()
                .filter(empleado -> empleado.obtenerCodigoEmpleado().equals(codigoEmpleado))
                .findFirst()
                .orElse(null);
    }

    public boolean verificarAutenticacionPin(CuentaBancaria cuenta, String pinIngresado) {
        return cuenta.obtenerClaveAcceso().equals(pinIngresado);
    }

    public boolean ejecutarTransferenciaMismoBanco(String tarjetaOrigen, String tarjetaDestino, double monto) {
        CuentaBancaria cuentaOrigen = localizarCuentaPorTarjeta(tarjetaOrigen);
        CuentaBancaria cuentaDestino = localizarCuentaPorTarjeta(tarjetaDestino);

        if (cuentaOrigen == null || cuentaDestino == null) return false;

        if (cuentaOrigen.procesarRetiro(monto)) {
            cuentaDestino.ejecutarDeposito(monto);
            cuentaDestino.registrarMovimiento(new RegistroTransaccion(
                    "Transferencia recibida", monto, "De tarjeta: " + tarjetaOrigen));
            return true;
        }
        return false;
    }

    public void agregarSolicitudTarjeta(SolicitudTarjetaCredito solicitud) {
        solicitudesTarjetas.add(solicitud);
    }

    public List<SolicitudTarjetaCredito> obtenerSolicitudesPendientes() {
        return solicitudesTarjetas.stream()
                .filter(s -> !s.estaAtendida())
                .collect(java.util.stream.Collectors.toList());
    }

    // Getters
    public String obtenerDenominacion() { return denominacionComercial; }
    public List<CuentaBancaria> obtenerCuentasActivas() { return registroCuentasActivas; }
    public List<ColaboradorBancario> obtenerPlantillaPersonal() { return plantillaPersonal; }
}

// Ventana principal del sistema
public class SistemaBancarioGUI extends JFrame {
    private InstitucionFinanciera banco1, banco2;
    private CuentaBancaria cuentaActual;
    private ColaboradorBancario empleadoActual;
    private InstitucionFinanciera bancoActual;

    public SistemaBancarioGUI() {
        inicializarDatos();
        inicializarInterfaz();
    }

    private void inicializarDatos() {
        // Crear bancos
        banco1 = new InstitucionFinanciera("Banco Nacional Digital");
        banco2 = new InstitucionFinanciera("Banco Internacional Plus");

        // Agregar cuentas de prueba
        banco1.incorporarCuentaNueva(new CuentaBancaria("Ana María González", "5512345678", 28, "GOMA280590",
                "Av. Reforma 123", 15000, "1234", "4532123456789012", 2001, CategoriaProducto.PREMIUM_INDIVIDUAL));
        banco1.incorporarCuentaNueva(new CuentaBancaria("Carlos Eduardo Ruiz", "5523456789", 22, "RUEC220399",
                "Calle Universidad 456", 3500, "5678", "4532234567890123", 2002, CategoriaProducto.JUVENIL_ESTUDIANTIL));

        banco2.incorporarCuentaNueva(new CuentaBancaria("María Elena Torres", "5556789012", 31, "TOME310187",
                "Colonia Roma 234", 12000, "7890", "4532567890123456", 2005, CategoriaProducto.PERSONAL_BASICA));

        // Agregar empleados
        banco1.incorporarColaborador(new ColaboradorBancario("Alejandra Supervisora", "SUP001", "Administrador General"));
        banco1.incorporarColaborador(new ColaboradorBancario("Miguel Ejecutivo", "EJE001", "Ejecutivo de Cuenta"));

        banco2.incorporarColaborador(new ColaboradorBancario("Fernando Director", "DIR002", "Administrador General"));
    }

    private void inicializarInterfaz() {
        setTitle("Sistema Bancario Digital");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Crear el panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Título
        JLabel titleLabel = new JLabel("PLATAFORMA BANCARIA DIGITAL", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Panel de opciones principales
        JPanel optionsPanel = new JPanel(new GridLayout(3, 1, 10, 10));

        JButton clienteBtn = new JButton("Acceso Cliente");
        JButton empleadoBtn = new JButton("Personal Autorizado");
        JButton salirBtn = new JButton("Salir del Sistema");

        clienteBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        empleadoBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        salirBtn.setFont(new Font("Arial", Font.PLAIN, 16));

        clienteBtn.addActionListener(e -> mostrarAccesoCliente());
        empleadoBtn.addActionListener(e -> mostrarAccesoEmpleado());
        salirBtn.addActionListener(e -> System.exit(0));

        optionsPanel.add(clienteBtn);
        optionsPanel.add(empleadoBtn);
        optionsPanel.add(salirBtn);

        mainPanel.add(optionsPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void mostrarAccesoCliente() {
        // Seleccionar banco
        String[] bancos = {banco1.obtenerDenominacion(), banco2.obtenerDenominacion()};
        String bancoSeleccionado = (String) JOptionPane.showInputDialog(this,
                "Seleccione su institución bancaria:",
                "Selección de Banco",
                JOptionPane.QUESTION_MESSAGE,
                null, bancos, bancos[0]);

        if (bancoSeleccionado == null) return;

        bancoActual = bancoSeleccionado.equals(banco1.obtenerDenominacion()) ? banco1 : banco2;

        // Ventana de login
        JDialog loginDialog = new JDialog(this, "Autenticación Cliente", true);
        loginDialog.setSize(400, 250);
        loginDialog.setLocationRelativeTo(this);

        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField tarjetaField = new JTextField(20);
        JPasswordField pinField = new JPasswordField(20);

        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(new JLabel("Número de tarjeta:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(tarjetaField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        loginPanel.add(new JLabel("PIN:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(pinField, gbc);

        JButton loginBtn = new JButton("Iniciar Sesión");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        loginPanel.add(loginBtn, gbc);

        loginBtn.addActionListener(e -> {
            String numTarjeta = tarjetaField.getText().trim();
            String pin = new String(pinField.getPassword());

            CuentaBancaria cuenta = bancoActual.localizarCuentaPorTarjeta(numTarjeta);
            if (cuenta != null && bancoActual.verificarAutenticacionPin(cuenta, pin)) {
                cuentaActual = cuenta;
                loginDialog.dispose();
                mostrarMenuCliente();
            } else {
                JOptionPane.showMessageDialog(loginDialog,
                        "Número de tarjeta o PIN incorrecto",
                        "Error de autenticación",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        loginDialog.add(loginPanel);
       
    }

    private void mostrarMenuCliente() {
        JFrame clienteFrame = new JFrame("Panel Cliente - " + cuentaActual.obtenerNombreTitular());
        clienteFrame.setSize(900, 700);
        clienteFrame.setLocationRelativeTo(this);
        clienteFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel de información
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Información de Cuenta"));
        infoPanel.add(new JLabel("Titular: " + cuentaActual.obtenerNombreTitular()));
        infoPanel.add(new JLabel("Tarjeta: ****" +
                cuentaActual.obtenerNumeroTarjeta().substring(cuentaActual.obtenerNumeroTarjeta().length() - 4)));

        JLabel saldoLabel = new JLabel("Saldo: $" + String.format("%.2f", cuentaActual.consultarSaldoActual()));
        saldoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        infoPanel.add(saldoLabel);

        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // Panel de operaciones
        JPanel operationsPanel = new JPanel(new GridLayout(4, 3, 10, 10));
        operationsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Botones de operaciones
        JButton depositoBtn = new JButton("Depósito");
        JButton retiroBtn = new JButton("Retiro");
        JButton transferirBtn = new JButton("Transferir");
        JButton crearInversionBtn = new JButton("Crear Inversión");
        JButton verInversionesBtn = new JButton("Ver Inversiones");
        JButton retirarInversionBtn = new JButton("Retirar Inversión");
        JButton historialBtn = new JButton("Historial");
        JButton solicitarTarjetaBtn = new JButton("Solicitar Tarjeta Crédito");
        JButton verTarjetaBtn = new JButton("Ver Mi Tarjeta");
        JButton cerrarBtn = new JButton("Cerrar Sesión");

        // Agregar listeners
        depositoBtn.addActionListener(e -> realizarDeposito(saldoLabel));
        retiroBtn.addActionListener(e -> realizarRetiro(saldoLabel));
        transferirBtn.addActionListener(e -> realizarTransferencia(saldoLabel));
        crearInversionBtn.addActionListener(e -> crearInversion(saldoLabel));
        verInversionesBtn.addActionListener(e -> mostrarInversiones());
        retirarInversionBtn.addActionListener(e -> retirarInversion(saldoLabel));
        historialBtn.addActionListener(e -> mostrarHistorial());
        solicitarTarjetaBtn.addActionListener(e -> solicitarTarjetaCredito());
        verTarjetaBtn.addActionListener(e -> mostrarTarjetaCredito());
        cerrarBtn.addActionListener(e -> clienteFrame.dispose());

        operationsPanel.add(depositoBtn);
        operationsPanel.add(retiroBtn);
        operationsPanel.add(transferirBtn);
        operationsPanel.add(crearInversionBtn);
        operationsPanel.add(verInversionesBtn);
        operationsPanel.add(retirarInversionBtn);
        operationsPanel.add(historialBtn);
        operationsPanel.add(solicitarTarjetaBtn);
        operationsPanel.add(verTarjetaBtn);
        operationsPanel.add(cerrarBtn);

        mainPanel.add(operationsPanel, BorderLayout.CENTER);
        clienteFrame.add(mainPanel);
        clienteFrame.setVisible(true);
    }

    private void realizarDeposito(JLabel saldoLabel) {
        String montoStr = JOptionPane.showInputDialog(this, "Ingrese el monto a depositar:");
        if (montoStr != null && !montoStr.trim().isEmpty()) {
            try {
                double monto = Double.parseDouble(montoStr);
                if (monto > 0) {
                    if (cuentaActual.ejecutarDeposito(monto)) {
                        cuentaActual.registrarMovimiento(new RegistroTransaccion(
                                "Depósito en efectivo", monto, "Depósito realizado por el titular"));
                        saldoLabel.setText("Saldo: $" + String.format("%.2f", cuentaActual.consultarSaldoActual()));
                        JOptionPane.showMessageDialog(this, "Depósito exitoso");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "El monto debe ser positivo");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Monto inválido");
            }
        }
    }

    private void realizarRetiro(JLabel saldoLabel) {
        String montoStr = JOptionPane.showInputDialog(this, "Ingrese el monto a retirar:");
        if (montoStr != null && !montoStr.trim().isEmpty()) {
            try {
                double monto = Double.parseDouble(montoStr);
                if (monto > 0) {
                    if (cuentaActual.procesarRetiro(monto)) {
                        cuentaActual.registrarMovimiento(new RegistroTransaccion(
                                "Retiro en efectivo", monto, "Retiro realizado por el titular"));
                        saldoLabel.setText("Saldo: $" + String.format("%.2f", cuentaActual.consultarSaldoActual()));
                        JOptionPane.showMessageDialog(this, "Retiro exitoso");
                    } else {
                        JOptionPane.showMessageDialog(this, "Fondos insuficientes");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "El monto debe ser positivo");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Monto inválido");
            }
        }
    }

    private void realizarTransferencia(JLabel saldoLabel) {
        JDialog transDialog = new JDialog(this, "Transferencia", true);
        transDialog.setSize(400, 200);
        transDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField tarjetaDestinoField = new JTextField(20);
        JTextField montoField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Tarjeta destino:"), gbc);
        gbc.gridx = 1;
        panel.add(tarjetaDestinoField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Monto:"), gbc);
        gbc.gridx = 1;
        panel.add(montoField, gbc);

        JButton transferirBtn = new JButton("Transferir");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(transferirBtn, gbc);

        transferirBtn.addActionListener(e -> {
            try {
                String tarjetaDestino = tarjetaDestinoField.getText().trim();
                double monto = Double.parseDouble(montoField.getText());

                if (bancoActual.ejecutarTransferenciaMismoBanco(
                        cuentaActual.obtenerNumeroTarjeta(), tarjetaDestino, monto)) {
                    cuentaActual.registrarMovimiento(new RegistroTransaccion(
                            "Transferencia enviada", monto, "A tarjeta: " + tarjetaDestino));
                    saldoLabel.setText("Saldo: $" + String.format("%.2f", cuentaActual.consultarSaldoActual()));
                    JOptionPane.showMessageDialog(transDialog, "Transferencia exitosa");
                    transDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(transDialog, "Error en la transferencia");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(transDialog, "Monto inválido");
            }
        });

        transDialog.add(panel);
        transDialog.setVisible(true);
    }

    private void crearInversion(JLabel saldoLabel) {
        String[] opciones = {
                "Mensual (30 días) - 4.8% anual",
                "Trimestral (90 días) - 6.2% anual",
                "Anual (365 días) - 10.5% anual"
        };

        String seleccion = (String) JOptionPane.showInputDialog(this,
                "Seleccione el tipo de inversión:",
                "Crear Inversión",
                JOptionPane.QUESTION_MESSAGE,
                null, opciones, opciones[0]);

        if (seleccion != null) {
            String montoStr = JOptionPane.showInputDialog(this, "Ingrese el monto a invertir:");
            if (montoStr != null && !montoStr.trim().isEmpty()) {
                try {
                    double monto = Double.parseDouble(montoStr);
                    if (monto > 0 && monto <= cuentaActual.consultarSaldoActual()) {
                        int plazo = 0;
                        double tasa = 0.0;

                        if (seleccion.contains("Mensual")) {
                            plazo = 30;
                            tasa = 0.048;
                        } else if (seleccion.contains("Trimestral")) {
                            plazo = 90;
                            tasa = 0.062;
                        } else if (seleccion.contains("Anual")) {
                            plazo = 365;
                            tasa = 0.105;
                        }

                        cuentaActual.procesarRetiro(monto);
                        ProductoInversion nuevaInversion = new ProductoInversion(monto, tasa, plazo);
                        cuentaActual.agregarProductoInversion(nuevaInversion);
                        cuentaActual.registrarMovimiento(new RegistroTransaccion("Nueva inversión", monto,
                                String.format("Inversión %.2f%% por %d días", tasa * 100, plazo)));

                        saldoLabel.setText("Saldo: $" + String.format("%.2f", cuentaActual.consultarSaldoActual()));
                        JOptionPane.showMessageDialog(this,
                                String.format("Inversión creada exitosamente.\nRendimiento proyectado: $%.2f",
                                        nuevaInversion.calcularRendimientoTotal()));
                    } else {
                        JOptionPane.showMessageDialog(this, "Monto inválido o fondos insuficientes");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Monto inválido");
                }
            }
        }
    }

    private void mostrarInversiones() {
        List<ProductoInversion> inversiones = cuentaActual.obtenerCarteraInversiones();

        if (inversiones.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No posee inversiones activas");
            return;
        }

        JFrame inversionesFrame = new JFrame("Cartera de Inversiones");
        inversionesFrame.setSize(600, 400);
        inversionesFrame.setLocationRelativeTo(this);

        String[] columnNames = {"#", "Monto", "Tasa", "Plazo (días)", "Rendimiento Proyectado"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        for (int i = 0; i < inversiones.size(); i++) {
            ProductoInversion inv = inversiones.get(i);
            Object[] row = {
                    i + 1,
                    String.format("$%.2f", inv.obtenerMontoCapital()),
                    String.format("%.2f%%", inv.obtenerPorcentajeRendimiento() * 100),
                    inv.obtenerDiasPlazo(),
                    String.format("$%.2f", inv.calcularRendimientoTotal())
            };
            model.addRow(row);
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        inversionesFrame.add(scrollPane);
        inversionesFrame.setVisible(true);
    }

    private void retirarInversion(JLabel saldoLabel) {
        List<ProductoInversion> inversiones = cuentaActual.obtenerCarteraInversiones();

        if (inversiones.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No posee inversiones activas para retirar");
            return;
        }

        String[] opciones = new String[inversiones.size()];
        for (int i = 0; i < inversiones.size(); i++) {
            ProductoInversion inv = inversiones.get(i);
            opciones[i] = String.format("Inversión %d: $%.2f - %.2f%% - %d días",
                    i + 1, inv.obtenerMontoCapital(),
                    inv.obtenerPorcentajeRendimiento() * 100, inv.obtenerDiasPlazo());
        }

        String seleccion = (String) JOptionPane.showInputDialog(this,
                "Seleccione la inversión a retirar:",
                "Retirar Inversión",
                JOptionPane.QUESTION_MESSAGE,
                null, opciones, opciones[0]);

        if (seleccion != null) {
            int index = 0;
            for (int i = 0; i < opciones.length; i++) {
                if (opciones[i].equals(seleccion)) {
                    index = i;
                    break;
                }
            }

            ProductoInversion inversionSeleccionada = inversiones.get(index);
            double montoFinal = inversionSeleccionada.calcularRendimientoTotal();

            cuentaActual.ejecutarDeposito(montoFinal);
            inversiones.remove(inversionSeleccionada);
            cuentaActual.registrarMovimiento(new RegistroTransaccion("Retiro de inversión", montoFinal,
                    "Inversión #" + (index + 1) + " liquidada"));

            saldoLabel.setText("Saldo: $" + String.format("%.2f", cuentaActual.consultarSaldoActual()));
            JOptionPane.showMessageDialog(this,
                    String.format("Inversión retirada exitosamente.\nMonto depositado: $%.2f", montoFinal));
        }
    }

    private void mostrarHistorial() {
        List<RegistroTransaccion> historial = cuentaActual.obtenerHistorialMovimientos();

        if (historial.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay movimientos registrados");
            return;
        }

        JFrame historialFrame = new JFrame("Historial de Movimientos");
        historialFrame.setSize(800, 500);
        historialFrame.setLocationRelativeTo(this);

        String[] columnNames = {"Fecha/Hora", "Tipo", "Monto", "Descripción"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        for (int i = historial.size() - 1; i >= 0; i--) {
            RegistroTransaccion transaccion = historial.get(i);
            Object[] row = {
                    transaccion.toString().substring(1, 20), // Fecha
                    transaccion.obtenerTipoMovimiento(),
                    String.format("$%.2f", transaccion.obtenerImporteOperacion()),
                    transaccion.obtenerDetalleDescriptivo()
            };
            model.addRow(row);
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        historialFrame.add(scrollPane);
        historialFrame.setVisible(true);
    }

    private void solicitarTarjetaCredito() {
        JDialog solicitudDialog = new JDialog(this, "Solicitar Tarjeta de Crédito", true);
        solicitudDialog.setSize(450, 300);
        solicitudDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField motivoField = new JTextField(20);
        JComboBox<String> tipoCombo = new JComboBox<>(new String[]{"Clásica", "Oro", "Platino"});
        JTextField montoField = new JTextField(20);
        JTextField ocupacionField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Motivo:"), gbc);
        gbc.gridx = 1;
        panel.add(motivoField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Tipo deseado:"), gbc);
        gbc.gridx = 1;
        panel.add(tipoCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Monto solicitado:"), gbc);
        gbc.gridx = 1;
        panel.add(montoField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Ocupación:"), gbc);
        gbc.gridx = 1;
        panel.add(ocupacionField, gbc);

        JButton enviarBtn = new JButton("Enviar Solicitud");
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(enviarBtn, gbc);

        enviarBtn.addActionListener(e -> {
            try {
                String motivo = motivoField.getText().trim();
                String tipo = (String) tipoCombo.getSelectedItem();
                double monto = Double.parseDouble(montoField.getText());
                String ocupacion = ocupacionField.getText().trim();

                if (!motivo.isEmpty() && !ocupacion.isEmpty() && monto > 0) {
                    SolicitudTarjetaCredito solicitud = new SolicitudTarjetaCredito(
                            cuentaActual.obtenerNombreTitular(), motivo,
                            cuentaActual.obtenerNumeroTarjeta(),
                            tipo, monto, ocupacion);

                    bancoActual.agregarSolicitudTarjeta(solicitud);
                    JOptionPane.showMessageDialog(solicitudDialog, "Solicitud enviada correctamente");
                    solicitudDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(solicitudDialog, "Complete todos los campos");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(solicitudDialog, "Monto inválido");
            }
        });

        solicitudDialog.add(panel);
        solicitudDialog.setVisible(true);
    }

    private void mostrarTarjetaCredito() {
        TarjetaCredito tarjeta = cuentaActual.obtenerTarjetaCredito();

        if (tarjeta == null) {
            JOptionPane.showMessageDialog(this, "No tienes una tarjeta de crédito activa");
            return;
        }

        String mensaje = String.format(
                "ESTADO DE TARJETA DE CRÉDITO\n\n" +
                        "Tipo: %s\n" +
                        "Límite: $%.2f\n" +
                        "Disponible: $%.2f\n" +
                        "Usado: $%.2f",
                tarjeta.getTipo(),
                tarjeta.getLimiteCredito(),
                tarjeta.getSaldoDisponible(),
                tarjeta.getLimiteCredito() - tarjeta.getSaldoDisponible()
        );

        JOptionPane.showMessageDialog(this, mensaje, "Mi Tarjeta de Crédito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarAccesoEmpleado() {
        // Seleccionar banco
        String[] bancos = {banco1.obtenerDenominacion(), banco2.obtenerDenominacion()};
        String bancoSeleccionado = (String) JOptionPane.showInputDialog(this,
                "Seleccione su institución bancaria:",
                "Selección de Banco",
                JOptionPane.QUESTION_MESSAGE,
                null, bancos, bancos[0]);

        if (bancoSeleccionado == null) return;

        bancoActual = bancoSeleccionado.equals(banco1.obtenerDenominacion()) ? banco1 : banco2;

        // Login empleado
        String codigoEmpleado = JOptionPane.showInputDialog(this, "Código de empleado:");
        if (codigoEmpleado != null) {
            ColaboradorBancario empleado = bancoActual.localizarEmpleadoPorCodigo(codigoEmpleado.trim());
            if (empleado != null) {
                empleadoActual = empleado;
                mostrarMenuEmpleado();
            } else {
                JOptionPane.showMessageDialog(this, "Código de empleado inválido");
            }
        }
    }

    private void mostrarMenuEmpleado() {
        JFrame empleadoFrame = new JFrame("Panel Empleado - " + empleadoActual.obtenerNombreCompleto());
        empleadoFrame.setSize(800, 600);
        empleadoFrame.setLocationRelativeTo(this);
        empleadoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel de información
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Información del Empleado"));
        infoPanel.add(new JLabel("Empleado: " + empleadoActual.obtenerNombreCompleto()));
        infoPanel.add(new JLabel("Cargo: " + empleadoActual.obtenerCargoDesempenado()));

        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // Panel de operaciones
        JPanel operationsPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        operationsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JButton verCuentasBtn = new JButton("Ver Cuentas");
        JButton verEmpleadosBtn = new JButton("Ver Empleados");
        JButton solicitudesBtn = new JButton("Solicitudes Tarjetas");
        JButton depositoEspecialBtn = new JButton("Depósito Especial");
        JButton bloquearCuentaBtn = new JButton("Bloquear/Desbloquear");
        JButton verClienteBtn = new JButton("Info Cliente");
        JButton agregarEmpleadoBtn = new JButton("Agregar Empleado");
        JButton reporteBtn = new JButton("Reporte General");
        JButton cerrarBtn = new JButton("Cerrar Sesión");

        // Listeners
        verCuentasBtn.addActionListener(e -> mostrarCuentasEmpleado());
        verEmpleadosBtn.addActionListener(e -> mostrarEmpleados());
        solicitudesBtn.addActionListener(e -> revisarSolicitudesTarjetas());
        depositoEspecialBtn.addActionListener(e -> realizarDepositoEspecial());
        bloquearCuentaBtn.addActionListener(e -> bloquearDesbloquearCuenta());
        verClienteBtn.addActionListener(e -> verInformacionCliente());
        agregarEmpleadoBtn.addActionListener(e -> agregarNuevoEmpleado());
        reporteBtn.addActionListener(e -> generarReporte());
        cerrarBtn.addActionListener(e -> empleadoFrame.dispose());

        operationsPanel.add(verCuentasBtn);
        operationsPanel.add(verEmpleadosBtn);
        operationsPanel.add(solicitudesBtn);
        operationsPanel.add(depositoEspecialBtn);
        operationsPanel.add(bloquearCuentaBtn);
        operationsPanel.add(verClienteBtn);
        operationsPanel.add(agregarEmpleadoBtn);
        operationsPanel.add(reporteBtn);
        operationsPanel.add(cerrarBtn);

        mainPanel.add(operationsPanel, BorderLayout.CENTER);
        empleadoFrame.add(mainPanel);
        empleadoFrame.setVisible(true);
    }

    private void mostrarCuentasEmpleado() {
        JFrame cuentasFrame = new JFrame("Cuentas Registradas - " + bancoActual.obtenerDenominacion());
        cuentasFrame.setSize(900, 500);
        cuentasFrame.setLocationRelativeTo(this);

        String[] columnNames = {"ID", "Titular", "Tarjeta", "Saldo", "Tipo", "Estado"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        for (CuentaBancaria cuenta : bancoActual.obtenerCuentasActivas()) {
            Object[] row = {
                    cuenta.obtenerIdCuenta(),
                    cuenta.obtenerNombreTitular(),
                    "****" + cuenta.obtenerNumeroTarjeta().substring(cuenta.obtenerNumeroTarjeta().length() - 4),
                    String.format("$%.2f", cuenta.consultarSaldoActual()),
                    cuenta.obtenerTipoProducto().toString().replace("_", " "),
                    cuenta.estaBloqueada() ? "Bloqueada" : "Activa"
            };
            model.addRow(row);
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        cuentasFrame.add(scrollPane);
        cuentasFrame.setVisible(true);
    }

    private void mostrarEmpleados() {
        JFrame empleadosFrame = new JFrame("Personal - " + bancoActual.obtenerDenominacion());
        empleadosFrame.setSize(600, 400);
        empleadosFrame.setLocationRelativeTo(this);

        String[] columnNames = {"Código", "Nombre", "Cargo"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        for (ColaboradorBancario empleado : bancoActual.obtenerPlantillaPersonal()) {
            Object[] row = {
                    empleado.obtenerCodigoEmpleado(),
                    empleado.obtenerNombreCompleto(),
                    empleado.obtenerCargoDesempenado()
            };
            model.addRow(row);
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        empleadosFrame.add(scrollPane);
        empleadosFrame.setVisible(true);
    }

    private void revisarSolicitudesTarjetas() {
        List<SolicitudTarjetaCredito> solicitudes = bancoActual.obtenerSolicitudesPendientes();

        if (solicitudes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay solicitudes pendientes");
            return;
        }

        JFrame solicitudesFrame = new JFrame("Solicitudes de Tarjetas de Crédito");
        solicitudesFrame.setSize(900, 600);
        solicitudesFrame.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());

        String[] columnNames = {"Cliente", "Tipo Deseado", "Monto", "Motivo", "Ocupación"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        for (SolicitudTarjetaCredito solicitud : solicitudes) {
            Object[] row = {
                    solicitud.getNombreCliente(),
                    solicitud.getTipoDeseado(),
                    String.format("$%.2f", solicitud.getMontoSolicitado()),
                    solicitud.getMotivo(),
                    solicitud.getOcupacion()
            };
            model.addRow(row);
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton aprobarBtn = new JButton("Aprobar Seleccionada");
        JButton rechazarBtn = new JButton("Rechazar Seleccionada");

        aprobarBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                procesarSolicitudTarjeta(solicitudes.get(selectedRow), true);
                model.removeRow(selectedRow);
            } else {
                JOptionPane.showMessageDialog(solicitudesFrame, "Seleccione una solicitud");
            }
        });

        rechazarBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                procesarSolicitudTarjeta(solicitudes.get(selectedRow), false);
                model.removeRow(selectedRow);
            } else {
                JOptionPane.showMessageDialog(solicitudesFrame, "Seleccione una solicitud");
            }
        });

        buttonPanel.add(aprobarBtn);
        buttonPanel.add(rechazarBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        solicitudesFrame.add(mainPanel);
        solicitudesFrame.setVisible(true);
    }

    private void procesarSolicitudTarjeta(SolicitudTarjetaCredito solicitud, boolean aprobar) {
        solicitud.marcarComoAtendida();

        if (aprobar) {
            String tipoStr = JOptionPane.showInputDialog(this,
                    "Tipo de tarjeta a asignar:", solicitud.getTipoDeseado());
            String limiteStr = JOptionPane.showInputDialog(this,
                    "Límite de crédito:", String.valueOf(solicitud.getMontoSolicitado()));

            if (tipoStr != null && limiteStr != null) {
                try {
                    double limite = Double.parseDouble(limiteStr);
                    CuentaBancaria cuenta = bancoActual.localizarCuentaPorTarjeta(solicitud.getNumeroTarjeta());
                    if (cuenta != null) {
                        cuenta.asignarTarjetaCredito(tipoStr, limite);
                        JOptionPane.showMessageDialog(this, "Tarjeta asignada correctamente");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Límite inválido");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Solicitud rechazada");
        }
    }

    private void realizarDepositoEspecial() {
        String numeroTarjeta = JOptionPane.showInputDialog(this, "Número de tarjeta destino:");
        if (numeroTarjeta != null) {
            CuentaBancaria cuenta = bancoActual.localizarCuentaPorTarjeta(numeroTarjeta.trim());
            if (cuenta != null) {
                String montoStr = JOptionPane.showInputDialog(this, "Monto a depositar:");
                if (montoStr != null) {
                    try {
                        double monto = Double.parseDouble(montoStr);
                        if (monto > 0) {
                            cuenta.ejecutarDeposito(monto);
                            cuenta.registrarMovimiento(new RegistroTransaccion("Depósito autorizado", monto,
                                    "Por: " + empleadoActual.obtenerNombreCompleto()));
                            JOptionPane.showMessageDialog(this, "Depósito realizado exitosamente");
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Monto inválido");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Cuenta no encontrada");
            }
        }
    }

    private void bloquearDesbloquearCuenta() {
        String numeroTarjeta = JOptionPane.showInputDialog(this, "Número de tarjeta:");
        if (numeroTarjeta != null) {
            CuentaBancaria cuenta = bancoActual.localizarCuentaPorTarjeta(numeroTarjeta.trim());
            if (cuenta != null) {
                String estado = cuenta.estaBloqueada() ? "bloqueada" : "activa";
                String accion = cuenta.estaBloqueada() ? "desbloquear" : "bloquear";

                int respuesta = JOptionPane.showConfirmDialog(this,
                        "La cuenta está " + estado + ". ¿Desea " + accion + "la?",
                        "Cambiar Estado",
                        JOptionPane.YES_NO_OPTION);

                if (respuesta == JOptionPane.YES_OPTION) {
                    if (cuenta.estaBloqueada()) {
                        cuenta.desbloquearCuenta();
                        JOptionPane.showMessageDialog(this, "Cuenta desbloqueada");
                    } else {
                        cuenta.bloquearCuenta();
                        JOptionPane.showMessageDialog(this, "Cuenta bloqueada");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Cuenta no encontrada");
            }
        }
    }

    private void verInformacionCliente() {
        String numeroTarjeta = JOptionPane.showInputDialog(this, "Número de tarjeta del cliente:");
        if (numeroTarjeta != null) {
            CuentaBancaria cuenta = bancoActual.localizarCuentaPorTarjeta(numeroTarjeta.trim());
            if (cuenta != null) {
                StringBuilder info = new StringBuilder();
                info.append("INFORMACIÓN DEL CLIENTE\n\n");
                info.append("Titular: ").append(cuenta.obtenerNombreTitular()).append("\n");
                info.append("Tarjeta: ****").append(numeroTarjeta.substring(numeroTarjeta.length() - 4)).append("\n");
                info.append("Saldo: $").append(String.format("%.2f", cuenta.consultarSaldoActual())).append("\n");
                info.append("Tipo: ").append(cuenta.obtenerTipoProducto().toString().replace("_", " ")).append("\n");
                info.append("Estado: ").append(cuenta.estaBloqueada() ? "Bloqueada" : "Activa").append("\n");
                info.append("Inversiones: ").append(cuenta.obtenerCarteraInversiones().size()).append("\n");

                TarjetaCredito tarjeta = cuenta.obtenerTarjetaCredito();
                if (tarjeta != null) {
                    info.append("Tarjeta Crédito: ").append(tarjeta.getTipo()).append("\n");
                    info.append("Límite: $").append(String.format("%.2f", tarjeta.getLimiteCredito()));
                } else {
                    info.append("Sin tarjeta de crédito");
                }

                JOptionPane.showMessageDialog(this, info.toString(), "Información Cliente", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Cuenta no encontrada");
            }
        }
    }

    private void agregarNuevoEmpleado() {
        if (!empleadoActual.obtenerCargoDesempenado().toLowerCase().contains("administrador")) {
            JOptionPane.showMessageDialog(this, "Solo los administradores pueden agregar empleados");
            return;
        }

        JDialog empleadoDialog = new JDialog(this, "Agregar Empleado", true);
        empleadoDialog.setSize(400, 250);
        empleadoDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField codigoField = new JTextField(20);
        JTextField nombreField = new JTextField(20);
        JTextField cargoField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Código:"), gbc);
        gbc.gridx = 1;
        panel.add(codigoField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Nombre completo:"), gbc);
        gbc.gridx = 1;
        panel.add(nombreField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Cargo:"), gbc);
        gbc.gridx = 1;
        panel.add(cargoField, gbc);

        JButton agregarBtn = new JButton("Agregar Empleado");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(agregarBtn, gbc);

        agregarBtn.addActionListener(e -> {
            String codigo = codigoField.getText().trim();
            String nombre = nombreField.getText().trim();
            String cargo = cargoField.getText().trim();

            if (!codigo.isEmpty() && !nombre.isEmpty() && !cargo.isEmpty()) {
                ColaboradorBancario nuevoEmpleado = new ColaboradorBancario(nombre, codigo, cargo);
                bancoActual.incorporarColaborador(nuevoEmpleado);
                JOptionPane.showMessageDialog(empleadoDialog, "Empleado agregado exitosamente");
                empleadoDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(empleadoDialog, "Complete todos los campos");
            }
        });

        empleadoDialog.add(panel);
        empleadoDialog.setVisible(true);
    }

    private void generarReporte() {
        JFrame reporteFrame = new JFrame("Reporte General - " + bancoActual.obtenerDenominacion());
        reporteFrame.setSize(800, 600);
        reporteFrame.setLocationRelativeTo(this);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        StringBuilder reporte = new StringBuilder();
        reporte.append("=".repeat(60)).append("\n");
        reporte.append("REPORTE GENERAL DEL BANCO\n");
        reporte.append("=".repeat(60)).append("\n\n");

        // Estadísticas generales
        List<CuentaBancaria> cuentas = bancoActual.obtenerCuentasActivas();
        List<ColaboradorBancario> empleados = bancoActual.obtenerPlantillaPersonal();

        reporte.append("ESTADÍSTICAS GENERALES:\n");
        reporte.append("- Total de cuentas: ").append(cuentas.size()).append("\n");
        reporte.append("- Total de empleados: ").append(empleados.size()).append("\n");

        double totalDepositos = cuentas.stream()
                .mapToDouble(CuentaBancaria::consultarSaldoActual)
                .sum();
        reporte.append("- Total en depósitos: $").append(String.format("%.2f", totalDepositos)).append("\n\n");

        // Detalle de cuentas
        reporte.append("CUENTAS REGISTRADAS:\n");
        reporte.append("-".repeat(40)).append("\n");
        for (CuentaBancaria cuenta : cuentas) {
            reporte.append("ID: ").append(cuenta.obtenerIdCuenta());
            reporte.append(" | Titular: ").append(cuenta.obtenerNombreTitular());
            reporte.append(" | Saldo: $").append(String.format("%.2f", cuenta.consultarSaldoActual()));
            reporte.append(" | Estado: ").append(cuenta.estaBloqueada() ? "Bloqueada" : "Activa");
            reporte.append("\n");
        }

        reporte.append("\nPERSONAL DEL BANCO:\n");
        reporte.append("-".repeat(40)).append("\n");
        for (ColaboradorBancario empleado : empleados) {
            reporte.append(empleado.toString()).append("\n");
        }

        // Solicitudes pendientes
        List<SolicitudTarjetaCredito> solicitudes = bancoActual.obtenerSolicitudesPendientes();
        reporte.append("\nSOLICITUDES PENDIENTES: ").append(solicitudes.size()).append("\n");
        reporte.append("-".repeat(40)).append("\n");
        for (SolicitudTarjetaCredito solicitud : solicitudes) {
            reporte.append("Cliente: ").append(solicitud.getNombreCliente());
            reporte.append(" | Tipo: ").append(solicitud.getTipoDeseado());
            reporte.append(" | Monto: $").append(String.format("%.2f", solicitud.getMontoSolicitado()));
            reporte.append("\n");
        }

        reporte.append("\n").append("=".repeat(60));

        textArea.setText(reporte.toString());
        JScrollPane scrollPane = new JScrollPane(textArea);
        reporteFrame.add(scrollPane);
        reporteFrame.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            // CORRECCIÓN: Usar getSystemLookAndFeelClassName() en lugar de getSystemLookAndFeel()
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new SistemaBancarioGUI().setVisible(true);
        });
    }
}