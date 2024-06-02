package control;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.DriverManagerConnectionPool;
import model.OrderModel;
import model.UserBean;

// Rimuovi altre importazioni non utilizzate

@WebServlet("/Login")
public class Login extends HttpServlet {
    // Rimuovi il codice non pertinente per la tua domanda

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Ottieni i parametri dalla richiesta
        String email = request.getParameter("j_email");
        String password = request.getParameter("j_password");
        String redirectedPage = "/loginPage.jsp";
        Boolean control = false;
        try {
            Connection con = DriverManagerConnectionPool.getConnection();
            String sql = "SELECT email, passwordUser, ruolo, nome, cognome, indirizzo, telefono, numero, intestatario, CVV FROM UserAccount WHERE email = ?";
            
            // Prepara la query
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, email);
            
            ResultSet rs = ps.executeQuery();

            // Se l'utente esiste nel database
            if (rs.next()) {
                // Crittografa la password inserita dall'utente
                String hashedPassword = checkPsw(password);
                
                // Confronta la password crittografata con quella memorizzata nel database
                if (hashedPassword.equals(rs.getString("passwordUser"))) {
                    control = true;
                    // Imposta l'utente registrato come attributo di sessione
                    UserBean registeredUser = new UserBean();
                    registeredUser.setEmail(rs.getString("email"));
                    registeredUser.setNome(rs.getString("nome"));
                    registeredUser.setCognome(rs.getString("cognome"));
                    registeredUser.setIndirizzo(rs.getString("indirizzo"));
                    registeredUser.setTelefono(rs.getString("telefono"));
                    registeredUser.setNumero(rs.getString("numero"));
                    registeredUser.setIntestatario(rs.getString("intestatario"));
                    registeredUser.setCvv(rs.getString("CVV"));
                    registeredUser.setRole(rs.getString("ruolo"));
                    request.getSession().setAttribute("registeredUser", registeredUser);
                    request.getSession().setAttribute("role", registeredUser.getRole());
                    request.getSession().setAttribute("email", rs.getString("email"));
                    request.getSession().setAttribute("nome", rs.getString("nome"));

                    // Carica gli ordini dell'utente
                    OrderModel model = new OrderModel();
                    request.getSession().setAttribute("listaOrdini", model.getOrders(rs.getString("email")));
                    
                    redirectedPage = "/index.jsp";
                }
            }
            DriverManagerConnectionPool.releaseConnection(con);
        }
        catch (SQLException e) {
            redirectedPage = "/loginPage.jsp";
        }
        if (!control) {
            request.getSession().setAttribute("login-error", true);
        }
        else {
            request.getSession().setAttribute("login-error", false);
        }
        response.sendRedirect(request.getContextPath() + redirectedPage);
    }
    
    // Metodo per crittografare la password utilizzando SHA-256
    private String checkPsw(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
