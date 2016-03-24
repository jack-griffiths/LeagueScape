package ardi.model.players.packets;

import ardi.model.players.Client;
import ardi.model.players.PacketType;
import ardi.model.players.PlayerHandler;
import ardi.model.players.lolspells.LeagueSpells;


/**
 * Walking packet
 **/
public class Walking implements PacketType {

	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		LeagueSpells.cancelVariablesOnWalk(c);
		if (c.usingAltar)
			c.usingAltar = false;
		if (c.canChangeAppearance) {
			c.canChangeAppearance = false;
		}
	
		if (c.teleporting) {
			c.startAnimation(65535);
			c.teleporting = false;
			c.isRunning = false;
			c.gfx0(-1);
			c.startAnimation(-1);
		}

		/*
		 * if (c.loggedIn == 0) { c.getDH().sendDialogues(691, 2244); return; }
		 */
		c.walkingToItem = false;
		c.isWc = false;
		c.clickNpcType = 0;
		c.clickObjectType = 0;
		if (c.isBanking)
			c.isBanking = false;
		if (c.tradeStatus >= 0) {
			c.tradeStatus = 0;
		}
		if (c.inTrade) {
			c.sendMessage("You must decline the trade to start walking.");
			return;
		}
	
		if (packetType == 248 || packetType == 164) {
			c.clickObjectType = 0;
			c.clickNpcType = 0;
			c.faceUpdate(0);
			c.npcIndex = 0;
			c.playerIndex = 0;
			if (c.followId > 0 || c.followId2 > 0)
				c.getPA().resetFollow();
		}
		if (c.canWalk == false) {
			return;
		}
		c.getPA().removeAllWindows();
		if (c.duelRule[1] && c.duelStatus == 5) {
			if (PlayerHandler.players[c.duelingWith] != null) {
				if (!c.goodDistance(c.getX(), c.getY(),
						PlayerHandler.players[c.duelingWith].getX(),
						PlayerHandler.players[c.duelingWith].getY(), 1)
						|| c.attackTimer == 0) {
					c.sendMessage("Walking has been disabled in this duel!");
				}
			}
			c.playerIndex = 0;
			return;
		}
		
		if (c.freezeTimer > 0) {
			if (PlayerHandler.players[c.playerIndex] != null) {
				if (c.goodDistance(c.getX(), c.getY(),
						PlayerHandler.players[c.playerIndex].getX(),
						PlayerHandler.players[c.playerIndex].getY(), 1)
						&& packetType != 98) {
					c.playerIndex = 0;
					return;
				}
			}
			if (packetType != 98) {
				c.sendMessage("A magical force stops you from moving.");
				c.playerIndex = 0;
			}
			return;
		}


		if (packetType == 248) {
			packetSize -= 14;
		}
		c.newWalkCmdSteps = (packetSize - 5) / 2;
		if (++c.newWalkCmdSteps > c.walkingQueueSize) {
			c.newWalkCmdSteps = 0;
			return;
		}

		c.getNewWalkCmdX()[0] = c.getNewWalkCmdY()[0] = 0;

		int firstStepX = c.getInStream().readSignedWordBigEndianA()
				- c.getMapRegionX() * 8;
		for (int i = 1; i < c.newWalkCmdSteps; i++) {
			c.getNewWalkCmdX()[i] = c.getInStream().readSignedByte();
			c.getNewWalkCmdY()[i] = c.getInStream().readSignedByte();
		}

		int firstStepY = c.getInStream().readSignedWordBigEndian()
				- c.getMapRegionY() * 8;
		c.setNewWalkCmdIsRunning((c.getInStream().readSignedByteC() == 1));
		// c.isResting = false;
		for (int i1 = 0; i1 < c.newWalkCmdSteps; i1++) {
			c.getNewWalkCmdX()[i1] += firstStepX;
			c.getNewWalkCmdY()[i1] += firstStepY;
		}
	}

}
