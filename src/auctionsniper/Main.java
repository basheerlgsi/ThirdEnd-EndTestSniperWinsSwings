package auctionsniper;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.SwingUtilities;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

public class Main{
	private static final int ARG_HOSTNAME = 0;
	private static final int ARG_USERNAME = 1;
	private static final int ARG_PASSWORD = 2;
	private static final int ARG_ITEM_ID = 3;
	public static final String AUCTION_RESOURCE = "Auction";
	public static final String ITEM_ID_AS_LOGIN = "auction-item-54321";
	public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/"
			+ AUCTION_RESOURCE;

	public static final String STATUS_JOINING = "Joining";
	public static final String STATUS_LOST = "Lost";
	public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;";
	public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;";
	public static final String CLOSE_EVENT_FORMAT ="SOLVersion: 1.1; Event: CLOSE;";
	public static final String BID_EVENT_FORMAT ="SOLVersion: 1.1; Event: PRICE; "+ "CurrentPrice: %d; Increment: %d; Bidder: %s;";	
	public static final String STATUS_BIDDING = "Bidding";
	public static final String STATUS_WINNING = "Winning";
	public static final String STATUS_WON = "WON";

	public static final String SNIPER_ID = "sniper";
	
	public static MainWindow ui;

	public Main() throws Exception {
		startUserInterface();
	}

	@SuppressWarnings("unused")
	private Chat notToBeGCd;
		
	private void joinAuction(XMPPConnection connection, String itemId)
			throws XMPPException {

		disconnectWhenUICloses(connection); // SASLAuthentication.bindResourceAndEstablishSession
		final Chat chat = connection.getChatManager().createChat("auction-item-54321@localhost", null);
		this.notToBeGCd = chat;

		Auction auction = new XMPPAuction(chat) ;
		chat.addMessageListener(new AuctionMessageTranslator(connection.getUser(),new AuctionSniper(auction, new SniperStateDisplayer())));

		auction.join();
	}
	
	public static class XMPPAuction implements Auction {
		private final Chat chat;

		public XMPPAuction(Chat chat) {
			this.chat = chat;
		}

		public void bid(int amount) {
			sendMessage(String.format(BID_COMMAND_FORMAT, amount));
		}

		public void join() {
			sendMessage(JOIN_COMMAND_FORMAT);
		}

		private void sendMessage(final String message) {
			try {
				chat.sendMessage(message);
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
	}


	private void disconnectWhenUICloses(final XMPPConnection connection) {
		ui.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				connection.disconnect();
			}
		});
		
	}

	private void startUserInterface() throws Exception {
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				ui = new MainWindow();
			}
		});
	}

	private static XMPPConnection connection(String hostname, String username,
			String password) throws XMPPException {
		XMPPConnection connection = new XMPPConnection(hostname);
		connection.connect();
		connection.login(username, password, AUCTION_RESOURCE);
		return connection;
	}

/*	private static String auctionId(String itemId, XMPPConnection connection) {
		
		return String.format(AUCTION_ID_FORMAT, itemId, connection
				.getServiceName());
	}
*/	
	public static void main(final String... args) throws Exception {
		
		final Main main = new Main();
	
		main.joinAuction(connection(args[ARG_HOSTNAME], args[ARG_USERNAME],
				args[ARG_PASSWORD]), args[ARG_ITEM_ID]);
	}
}