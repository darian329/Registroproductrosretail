import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.sql.*;

public class SistemaCaja extends JFrame {

    private JTextField txtCodigoBusqueda, txtCantidad, txtDineroRecibido;
    private JLabel lblNombreProd, lblPrecioProd, lblStockProd, lblTotalPagar;
    private JTable tablaProductosCaja;
    private DefaultTableModel modeloTablaCaja;
    private JButton btnRealizarVenta, btnLimpiarCaja;
    
    private BigDecimal precioActual = BigDecimal.ZERO;
    private int stockActual = 0;
    private String nombreActual = "";
    private String codigoActual = ""; // Código del producto mostrado (el que se vende)

    public SistemaCaja() {
        setTitle("Sistema de Caja - Supermercado (Vendedor)");
        setSize(750, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel superior para caja
        JPanel panelVenta = new JPanel(new GridBagLayout());
        panelVenta.setBorder(BorderFactory.createTitledBorder("Caja Registradora - Cobro"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panelVenta.add(new JLabel("Código (escanee o escriba):"), gbc);
        gbc.gridx = 1; txtCodigoBusqueda = new JTextField(12); panelVenta.add(txtCodigoBusqueda, gbc);

        gbc.gridx = 2; gbc.gridy = 0; panelVenta.add(new JLabel("Cantidad:"), gbc);
        gbc.gridx = 3; txtCantidad = new JTextField(8); txtCantidad.setText("1"); panelVenta.add(txtCantidad, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panelVenta.add(new JLabel("Producto:"), gbc);
        lblNombreProd = new JLabel("---");
        lblNombreProd.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 1; panelVenta.add(lblNombreProd, gbc);

        gbc.gridx = 2; gbc.gridy = 1; panelVenta.add(new JLabel("Precio Unit.:"), gbc);
        lblPrecioProd = new JLabel("$0");
        gbc.gridx = 3; panelVenta.add(lblPrecioProd, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panelVenta.add(new JLabel("Stock Disp.:"), gbc);
        lblStockProd = new JLabel("0");
        gbc.gridx = 1; panelVenta.add(lblStockProd, gbc);

        gbc.gridx = 2; gbc.gridy = 2; panelVenta.add(new JLabel("Total a Pagar:"), gbc);
        lblTotalPagar = new JLabel("$0");
        lblTotalPagar.setForeground(new Color(192, 57, 43));
        lblTotalPagar.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 3; panelVenta.add(lblTotalPagar, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panelVenta.add(new JLabel("Dinero Recibido ($):"), gbc);
        gbc.gridx = 1; txtDineroRecibido = new JTextField(12); panelVenta.add(txtDineroRecibido, gbc);

        // Botones
        JPanel panelBotonesCaja = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnRealizarVenta = new JButton("Cobrar / Vender");
        btnRealizarVenta.setBackground(new Color(46, 204, 113));
        btnRealizarVenta.setForeground(Color.WHITE);
        btnRealizarVenta.setFont(new Font("Arial", Font.BOLD, 12));

        btnLimpiarCaja = new JButton("Limpiar");
        panelBotonesCaja.add(btnRealizarVenta);
        panelBotonesCaja.add(btnLimpiarCaja);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4;
        panelVenta.add(panelBotonesCaja, gbc);

        add(panelVenta, BorderLayout.NORTH);

        // Tabla de inventario para el cajero
        String[] columnas = {"Código", "Nombre", "Categoría", "Precio", "Stock"};
        modeloTablaCaja = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tablaProductosCaja = new JTable(modeloTablaCaja);
        add(new JScrollPane(tablaProductosCaja), BorderLayout.CENTER);

        // Eventos
        txtCodigoBusqueda.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                buscarProductoPorCodigo(txtCodigoBusqueda.getText().trim());
            }
        });

        txtCantidad.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calcularTotal();
            }
        });

        // El lector de código de barras funciona como teclado: escribe el código y envía Enter.
        // Enter avanza: código -> cantidad -> dinero recibido -> cobrar
        txtCodigoBusqueda.addActionListener(e -> buscarPorEscaneo());
        txtCodigoBusqueda.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                // Seleccionado: el próximo escaneo reemplaza el código anterior en vez de sumarse
                SwingUtilities.invokeLater(txtCodigoBusqueda::selectAll);
            }
        });
        txtCantidad.addActionListener(e -> txtDineroRecibido.requestFocusInWindow());
        txtDineroRecibido.addActionListener(e -> ejecutarVenta());

        btnRealizarVenta.addActionListener(e -> ejecutarVenta());
        btnLimpiarCaja.addActionListener(e -> limpiarCaja());

        tablaProductosCaja.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int fila = tablaProductosCaja.getSelectedRow();
                if (fila >= 0) {
                    String codigoSel = modeloTablaCaja.getValueAt(fila, 0).toString();
                    txtCodigoBusqueda.setText(codigoSel);
                    buscarProductoPorCodigo(codigoSel);
                }
            }
        });

        cargarInventarioCaja();
    }

    private void cargarInventarioCaja() {
        modeloTablaCaja.setRowCount(0);
        String sql = "SELECT * FROM productos";
        try (Connection con = Conexion.obtenerConexion();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] fila = {
                    rs.getString("codigo"),
                    rs.getString("nombre"),
                    rs.getString("categoria"),
                    Moneda.formatear(rs.getBigDecimal("precio")),
                    rs.getInt("stock")
                };
                modeloTablaCaja.addRow(fila);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el inventario.\nVerifique que Laragon (MySQL) esté encendido.\n\nDetalle: " + e.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscarProductoPorCodigo(String codigo) {
        if (codigo.isEmpty()) {
            limpiarDatosProducto();
            return;
        }

        String sql = "SELECT * FROM productos WHERE codigo = ?";
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                codigoActual = rs.getString("codigo");
                nombreActual = rs.getString("nombre");
                precioActual = rs.getBigDecimal("precio");
                stockActual = rs.getInt("stock");

                lblNombreProd.setText(nombreActual);
                lblPrecioProd.setText(Moneda.formatear(precioActual));
                lblStockProd.setText(String.valueOf(stockActual));
                calcularTotal();
            } else {
                limpiarDatosProducto();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void buscarPorEscaneo() {
        String codigo = txtCodigoBusqueda.getText().trim();
        if (codigo.isEmpty()) return;

        buscarProductoPorCodigo(codigo);
        if (codigoActual.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No existe un producto con el código " + codigo + ".", "Producto no encontrado", JOptionPane.WARNING_MESSAGE);
            txtCodigoBusqueda.requestFocusInWindow();
            txtCodigoBusqueda.selectAll();
            return;
        }
        txtCantidad.requestFocusInWindow();
        txtCantidad.selectAll();
    }

    private void calcularTotal() {
        try {
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            BigDecimal total = cantidad > 0 ? precioActual.multiply(BigDecimal.valueOf(cantidad)) : BigDecimal.ZERO;
            lblTotalPagar.setText(Moneda.formatear(total));
        } catch (NumberFormatException e) {
            lblTotalPagar.setText("$0");
        }
    }

    private void ejecutarVenta() {
        // Se vuelve a buscar el producto escrito, para vender con su precio y stock actuales
        buscarProductoPorCodigo(txtCodigoBusqueda.getText().trim());
        String codigo = codigoActual;
        if (codigo.isEmpty() || nombreActual.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un código de producto válido.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int cantidadVenta = Integer.parseInt(txtCantidad.getText().trim());
            BigDecimal dineroRecibido = Moneda.leer(txtDineroRecibido.getText());
            if (dineroRecibido == null || Moneda.tieneMasDeDosDecimales(dineroRecibido)) {
                JOptionPane.showMessageDialog(this, "Escriba el dinero recibido solo con números, por ejemplo 5000 o 5.000.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (cantidadVenta <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (cantidadVenta > stockActual) {
                JOptionPane.showMessageDialog(this, "Stock insuficiente. Disponible: " + stockActual, "Stock", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BigDecimal totalPagar = precioActual.multiply(BigDecimal.valueOf(cantidadVenta));

            if (dineroRecibido.compareTo(totalPagar) < 0) {
                JOptionPane.showMessageDialog(this, "El dinero recibido es insuficiente. Total: " + Moneda.formatear(totalPagar), "Dinero Insuficiente", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BigDecimal vuelto = dineroRecibido.subtract(totalPagar);

            // Un vuelto muy alto suele ser un error de digitación o un código escaneado en este campo
            if (vuelto.compareTo(new BigDecimal("100000")) >= 0) {
                int r = JOptionPane.showConfirmDialog(this,
                        "Dinero recibido: " + Moneda.formatear(dineroRecibido) + "\nVuelto: " + Moneda.formatear(vuelto) + "\n\n¿Es correcto?",
                        "Revise el dinero recibido", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (r != JOptionPane.YES_OPTION) {
                    txtDineroRecibido.requestFocusInWindow();
                    txtDineroRecibido.selectAll();
                    return;
                }
            }

            // Resta sobre el stock real y solo si alcanza
            String sql = "UPDATE productos SET stock = stock - ? WHERE codigo = ? AND stock >= ?";
            try (Connection con = Conexion.obtenerConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, cantidadVenta);
                ps.setString(2, codigo);
                ps.setInt(3, cantidadVenta);
                if (ps.executeUpdate() == 0) {
                    JOptionPane.showMessageDialog(this, "No se pudo vender: el producto ya no tiene stock suficiente.", "Stock", JOptionPane.ERROR_MESSAGE);
                    cargarInventarioCaja();
                    buscarProductoPorCodigo(codigo);
                    return;
                }

                String ticket = "=== TICKET DE VENTA (CAJA) ===\n" +
                                "Producto: " + nombreActual + "\n" +
                                "Código: " + codigo + "\n" +
                                "Cantidad: " + cantidadVenta + "\n" +
                                "Precio Unit.: " + Moneda.formatear(precioActual) + "\n" +
                                "-----------------------------------\n" +
                                "Total a Pagar: " + Moneda.formatear(totalPagar) + "\n" +
                                "Dinero Recibido: " + Moneda.formatear(dineroRecibido) + "\n" +
                                "Vuelto: " + Moneda.formatear(vuelto) + "\n" +
                                "===================================";

                JOptionPane.showMessageDialog(this, ticket, "Venta Exitosa", JOptionPane.INFORMATION_MESSAGE);

                cargarInventarioCaja();
                limpiarCaja();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error en base de datos: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Escriba la cantidad como un número entero, por ejemplo 1 o 2.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarDatosProducto() {
        codigoActual = "";
        nombreActual = "";
        precioActual = BigDecimal.ZERO;
        stockActual = 0;
        lblNombreProd.setText("---");
        lblPrecioProd.setText("$0");
        lblStockProd.setText("0");
        lblTotalPagar.setText("$0");
    }

    private void limpiarCaja() {
        txtCodigoBusqueda.setText("");
        txtCantidad.setText("1");
        txtDineroRecibido.setText("");
        limpiarDatosProducto();
        tablaProductosCaja.clearSelection();
        txtCodigoBusqueda.requestFocusInWindow(); // Listo para escanear el siguiente producto
    }

    // MAIN INDEPENDIENTE PARA EL VENDEDOR
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SistemaCaja().setVisible(true));
    }
}