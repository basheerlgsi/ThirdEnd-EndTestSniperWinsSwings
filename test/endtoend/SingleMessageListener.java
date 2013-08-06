package endtoend;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SingleMessageListener implements MessageListener {
	private final ArrayBlockingQueue<Message> messages = 
		new ArrayBlockingQueue<Message>(1);

	public void processMessage(Chat chat, Message message) {
		messages.add(message);
	}

	public void receivesAMessage(Matcher<? super String> messageMatcher) throws InterruptedException {
		final Message message = messages.poll(5, TimeUnit.SECONDS);

		assertThat("Message", message, is(notNullValue()));
		
		System.out.println("\nSingleMessageListener::ReceieveAMessage"+message.getBody());
		assertThat(message.getBody(), is(notNullValue()));
		assertThat(message.getBody(), messageMatcher);
		
	}
}