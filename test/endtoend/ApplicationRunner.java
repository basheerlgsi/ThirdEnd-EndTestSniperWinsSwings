package endtoend;

import auctionsniper.Main;

public class ApplicationRunner {

	public static final String SNIPER_ID = "sniper";
	public static final String SNIPER_PASSWORD = "sniper";
	public static final String SNIPER_XMPP_ID = "sniper@localhost/Auction";
	private AuctionSniperDriver driver;
	public Thread thread;
	public ApplicationRunner()
	{
		
	}
	
	public void startBiddingIn(final FakeAuctionServer auction){
			thread = new Thread("Test Application"){
			@Override
			public void run() {
				try {
					Main.main(FakeAuctionServer.XMPP_HOSTNAME, SNIPER_ID,
							SNIPER_PASSWORD, auction.getItemId());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		thread.setDaemon(true);
		thread.start();
		driver = new AuctionSniperDriver(1000);
		driver.showsSniperStatus(Main.STATUS_JOINING);
	}

	public void showsSniperHasLostAuction() {
		driver.showsSniperStatus(Main.STATUS_LOST);
	}

	public void stop() throws InterruptedException {
		if (driver != null) {
			driver.dispose();
		}

	}

	public void hasShownSniperIsBidding() {
		driver.showsSniperStatus(Main.STATUS_BIDDING);
	}

	public void hasShownSniperIsWinning() {
		driver.showsSniperStatus(Main.STATUS_WINNING);
		
	}

	public void showsSniperHasWonAuction() {
		driver.showsSniperStatus(Main.STATUS_WON);
		
	}

}