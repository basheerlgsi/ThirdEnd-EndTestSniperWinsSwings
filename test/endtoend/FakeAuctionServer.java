package endtoend;

import org.hamcrest.Matcher;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import auctionsniper.Main;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FakeAuctionServer {

	public static final String ITEM_ID_AS_LOGIN = "auction-%s";
	public static final String AUCTION_RESOURCE = "Auction";
	public static final String XMPP_HOSTNAME = "localhost";
	private static final String AUCTION_PASSWORD = "auction";
	private final String itemId;
	private final XMPPConnection connection;
	private Chat currentChat;
	private final SingleMessageListener messageListener = new SingleMessageListener();

	public FakeAuctionServer(String itemId) {
		this.itemId = itemId;
		ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(
				XMPP_HOSTNAME, 5222);
		this.connection = new XMPPConnection(connectionConfiguration);
	}

	public void startSellingItem() throws XMPPException {
		connection.connect();
		connection.login(String.format(ITEM_ID_AS_LOGIN, itemId),
				AUCTION_PASSWORD, AUCTION_RESOURCE);
		connection.getChatManager().addChatListener(new ChatManagerListener() {
			public void chatCreated(Chat chat, boolean createdLocally) {
				currentChat = chat;
				chat.addMessageListener(messageListener);
			}
		});
	}

	public void hasReceivedJoinRequestFromSniper() throws InterruptedException, XMPPException {
		//currentChat.sendMessage(new Message());
		//messageListener.receivesAMessage();//is(anything()));
		messageListener.receivesAMessage(equalTo(Main.JOIN_COMMAND_FORMAT));//is(anything()));
		
	}
	
	public void hasReceivedJoinRequestFromSniper(String sniperid) throws InterruptedException {
		receivesAMessageMatching(sniperid, equalTo(Main.JOIN_COMMAND_FORMAT));
	}

	
	private void receivesAMessageMatching(String sniperid,
			Matcher<? super String> messageMatcher) throws InterruptedException{	
		messageListener.receivesAMessage(messageMatcher);
		assertThat(currentChat.getParticipant(), equalTo(sniperid));
	}

	public void announceClosed() throws XMPPException {
		Message message= new Message();
		message.setBody((Main.CLOSE_EVENT_FORMAT));
		currentChat.sendMessage(message);//Main.CLOSE_EVENT_FORMAT);
	}

	public void stop() {
		connection.disconnect();
	}

	public String getItemId() {
		return itemId;
	}

	public void reportPrice(int price, int increment, String bidder)
			throws XMPPException {
		Message message= new Message();
		message.setBody(String.format(Main.BID_EVENT_FORMAT, price,increment, bidder));
		currentChat.sendMessage(message);

	}

	public void hasReceivedBid(int bid, String sniperXmppId) throws InterruptedException {
		receivesAMessageMatching(sniperXmppId,
				equalTo(String.format(Main.BID_COMMAND_FORMAT, bid)));
		
	}
}