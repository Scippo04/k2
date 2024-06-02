/*
Il problema che stai riscontrando è dovuto alla mancanza di controllo sui parametri inviati alla servlet. 
Quando la servlet cerca di convertire una stringa vuota in un intero (usando Integer.parseInt), 
si verifica un'eccezione NumberFormatException.
Per risolvere questo problema e prevenire ulteriori attacchi di manomissione dei parametri,
 è necessario aggiungere controlli sui parametri. Di seguito, ho apportato le modifiche al 
 tuo codice per includere questi controlli e migliorare la gestione delle eccezioni.
 */

package control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.CartBean;
import model.CartModel;
import model.PreferitiModel;
import model.ProductBean;
import model.ProductModel;

@WebServlet("/ProductControl")
/**
 * Servlet implementation class ProductControl
 */
public class ProductControl extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	static ProductModel model;
	
	static {
		model = new ProductModel();
	}
	
	public ProductControl() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String action = request.getParameter("action");
		if (action != null) {
			try {
				switch (action) {
					case "dettaglio":
						handleDetail(request, response);
						break;
					case "elimina":
						handleDelete(request, response);
						break;
					case "modificaForm":
						handleModifyForm(request, response);
						break;
					case "modifica":
						handleModify(request, response);
						break;
					default:
						handleDefault(request, response);
						break;
				}
			} catch (NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid number format.");
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
			}
		} else {
			try {
				handleDefault(request, response);
			} catch (SQLException | ServletException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void handleDetail(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, ServletException, IOException {
		String codiceStr = request.getParameter("codice");
		if (isValidInteger(codiceStr)) {
			int codice = Integer.parseInt(codiceStr);
			ProductBean prodotto = model.doRetrieveByKey(codice);
			request.setAttribute("prodottoDettaglio", prodotto);
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/productDetail.jsp");
			dispatcher.forward(request, response);
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid product code.");
		}
	}

	private void handleDelete(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, ServletException, IOException {
		@SuppressWarnings("unchecked")
		Collection<ProductBean> lista = (Collection<ProductBean>) request.getSession().getAttribute("products");
		String codiceStr = request.getParameter("codice");
		if (isValidInteger(codiceStr)) {
			int codice = Integer.parseInt(codiceStr);
			Collection<ProductBean> collezione = model.deleteProduct(codice, lista);
			request.getSession().setAttribute("products", collezione);
			request.getSession().setAttribute("refreshProduct", true);
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/ProductsPage.jsp");
			dispatcher.forward(request, response);
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid product code.");
		}
	}

	private void handleModifyForm(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, ServletException, IOException {
		String codiceStr = request.getParameter("codice");
		if (isValidInteger(codiceStr)) {
			int codice = Integer.parseInt(codiceStr);
			ProductBean bean = model.doRetrieveByKey(codice);
			request.setAttribute("updateProd", bean);
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/modifica-prodotto.jsp"); 
			dispatcher.forward(request, response);
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid product code.");
		}
	}

	private void handleModify(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, ServletException, IOException {
		String codiceStr = request.getParameter("codice");
		if (isValidInteger(codiceStr)) {
			int codice = Integer.parseInt(codiceStr);
			ProductBean bean = new ProductBean();
			bean.setCodice(codice);
			bean.setNome(request.getParameter("nome"));
			bean.setDescrizione(request.getParameter("descrizione"));
			bean.setPrezzo(Double.parseDouble(request.getParameter("prezzo")));
			bean.setSpedizione(Double.parseDouble(request.getParameter("spedizione")));
			bean.setTag(request.getParameter("tag"));
			bean.setTipologia(request.getParameter("tipologia"));

			model.updateProduct(bean);
			updateCartAndFavorites(request, bean);
			request.getSession().setAttribute("refreshProduct", true);
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp"); 
			dispatcher.forward(request, response);
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid product code.");
		}
	}

	private void handleDefault(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, ServletException, IOException {
		String tipologia = (String) request.getSession().getAttribute("tipologia");
		request.removeAttribute("products");
		request.setAttribute("products", model.doRetrieveAll(tipologia));
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/ProductsPage.jsp?tipologia=" + tipologia);
		dispatcher.forward(request, response);
	}

	private boolean isValidInteger(String str) {
		if (str == null || str.isEmpty()) {
			return false;
		}
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private void updateCartAndFavorites(HttpServletRequest request, ProductBean bean) throws SQLException {
		if (request.getSession().getAttribute("carrello") != null) {
			CartModel cartmodel = new CartModel();
			CartBean newCart = cartmodel.updateCarrello(bean, (CartBean) request.getSession().getAttribute("carrello"));
			request.getSession().setAttribute("carrello", newCart);
		}
		if (request.getSession().getAttribute("preferiti") != null) {
			PreferitiModel preferitiModel = new PreferitiModel();
			@SuppressWarnings("unchecked")
			Collection<ProductBean> lista = preferitiModel.updatePreferiti(bean, (Collection<ProductBean>) request.getSession().getAttribute("preferiti"));
			request.getSession().setAttribute("preferiti", lista);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}
