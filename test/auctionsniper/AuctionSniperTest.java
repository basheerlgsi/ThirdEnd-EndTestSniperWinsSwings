package auctionsniper;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.States;
import org.junit.Test;

import auctionsniper.AuctionEventListener.PriceSource;

public class AuctionSniperTest {
	private final Mockery context = new Mockery();
	private final SniperListener sniperListener = context
			.mock(SniperListener.class);
	private final Auction auction = context.mock(Auction.class);
	private final AuctionSniper sniper = new AuctionSniper(auction,
			sniperListener);

	private final States sniperState = context.states("sniper"); //1
	
	@Test
	public void reportsLostWhenAuctionClosesImmediately() { //2
		context.checking(new Expectations() {
			{
				one(sniperListener).sniperLost();
			}
		});
		sniper.auctionClosed();
	}

	@Test
	public void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
		final int price = 1001;
		final int increment = 25;
		context.checking(new Expectations() {
			{
				one(auction).bid(price + increment);
				atLeast(1).of(sniperListener).sniperBidding();
			}
		});
		sniper.currentPrice(price, increment,PriceSource.FromOtherBidder);
	}
	
	@Test
	public void reportsIsWinningWhenCurrentPriceComesFromSniper() {
		context.checking(new Expectations() {
			{
				atLeast(1).of(sniperListener).sniperWinning();
			}
		});
		sniper.currentPrice(123, 45, PriceSource.FromSniper);
	}

	@Test 
	public void
	reportsLostIfAuctionClosesWhenBidding() {
		context.checking(new Expectations() {
			{
				ignoring(auction); //3
				allowing(sniperListener).sniperBidding();
												then(sniperState.is("bidding")); //4
				atLeast(1).of(sniperListener).sniperLost();
												when(sniperState.is("bidding")); //5
			}
		});
		
		sniper.currentPrice(123, 45, PriceSource.FromOtherBidder); //6
		sniper.auctionClosed();
	}
	
	
	@Test 
	public 
	void reportsWonIfAuctionClosesWhenWinning() {
		context.checking(new Expectations() {
			{
				ignoring(auction);
				allowing(sniperListener).sniperWinning();
												then(sniperState.is("winning"));
				atLeast(1).of(sniperListener).sniperWon();
												when(sniperState.is("winning"));
			}
		});
		
		sniper.currentPrice(123, 45, PriceSource.FromSniper);
		sniper.auctionClosed();
	}
}
