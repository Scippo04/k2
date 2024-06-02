package control;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.DriverManagerConnectionPool;

// Rimuovi altre importazioni non utilizzate

@WebServlet("/Register")
public class Register extends HttpServlet {
    // Rimuovi il codice non pertinente per la tua domanda

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Ottieni i parametri dalla richiesta
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String nome = request.getParameter("nome");
        String cognome = request.getParameter("cognome");
        String indirizzo = request.getParameter("indirizzo");
        String telefono = request.getParameter("telefono");
        String carta = request.getParameter("carta");
        String intestatario = request.getParameter("intestatario");
        String cvv = request.getParameter("cvv");
        String redirectedPage = "/loginPage.jsp";
        try {
            // Crea una connessione al database
            Connection con = DriverManagerConnectionPool.getConnection();
            String sql = "INSERT INTO UserAccount(email, passwordUser, nome, cognome, indirizzo, telefono, numero, intestatario, CVV) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // Crittografa la password utilizzando SHA-256
            String hashedPassword = hashPassword(password);

            // Esegui la query di inserimento utilizzando la password crittografata
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, hashedPassword);
            ps.setString(3, nome);
            ps.setString(4, cognome);
            ps.setString(5, indirizzo);
            ps.setString(6, telefono);
            ps.setString(7, carta);
            ps.setString(8, intestatario);
            ps.setString(9, cvv);

            ps.executeUpdate();
            con.commit();
            DriverManagerConnectionPool.releaseConnection(con);
        } catch (SQLException e) {
            // Gestisci eventuali eccezioni
            request.getSession().setAttribute("register-error", true);
            redirectedPage = "/register-form.jsp";
        } catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // Reindirizza alla pagina appropriata
        response.sendRedirect(request.getContextPath() + redirectedPage);
    }

    // Metodo per crittografare la password utilizzando SHA-256
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(password.getBytes());
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : hashBytes) {
            stringBuilder.append(String.format("%02x", b));
        }
        return stringBuilder.toString();
    }
}
