package mage.abilities.effects.common;

import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.Game;
import mage.players.Player;
import mage.util.CardUtil;

/**
 *
 * @author LevelX, awjackson
 */
public class LookLibraryControllerEffect extends OneShotEffect {

    public enum PutCards {
        HAND(Outcome.DrawCard, Zone.HAND, "into your hand"),
        GRAVEYARD(Outcome.Discard, Zone.GRAVEYARD, "into your graveyard"),
        BATTLEFIELD(Outcome.PutCardInPlay, Zone.BATTLEFIELD, "onto the battlefield"),
        BATTLEFIELD_TAPPED(Outcome.PutCardInPlay, Zone.BATTLEFIELD, "onto the battlefield tapped"),
        TOP_ANY(Outcome.Benefit, Zone.LIBRARY, "on top of your library", " in any order"),
        BOTTOM_ANY(Outcome.Benefit, Zone.LIBRARY, "on the bottom of your library", " in any order"),
        BOTTOM_RANDOM(Outcome.Benefit, Zone.LIBRARY, "on the bottom of your library", " in a random order");

        private final Outcome outcome;
        private final Zone zone;
        private final String message;
        private final String order;

        PutCards(Outcome outcome, Zone zone, String message) {
            this(outcome, zone, message, "");
        }

        PutCards(Outcome outcome, Zone zone, String message, String order) {
            this.outcome = outcome;
            this.zone = zone;
            this.message = message;
            this.order = order;
        }

        public Outcome getOutcome() {
            return outcome;
        }

        public Zone getZone() {
            return zone;
        }

        public String getMessage(boolean withOrder) {
            return withOrder ? message + order : message;
        }
    }

    protected DynamicValue numberOfCards;
    protected PutCards putLookedCards;
    protected boolean revealCards;

    public LookLibraryControllerEffect() {
        this(1);
    }

    public LookLibraryControllerEffect(int numberOfCards) {
        this(StaticValue.get(numberOfCards));
    }

    public LookLibraryControllerEffect(DynamicValue numberOfCards) {
        this(Outcome.Benefit, numberOfCards, PutCards.TOP_ANY);
    }

    public LookLibraryControllerEffect(Outcome outcome, DynamicValue numberOfCards, PutCards putLookedCards) {
        super(outcome);
        this.numberOfCards = numberOfCards;
        this.putLookedCards = putLookedCards;
        this.revealCards = false;
    }

    public LookLibraryControllerEffect(final LookLibraryControllerEffect effect) {
        super(effect);
        this.numberOfCards = effect.numberOfCards.copy();
        this.putLookedCards = effect.putLookedCards;
        this.revealCards = effect.revealCards;
    }

    @Override
    public LookLibraryControllerEffect copy() {
        return new LookLibraryControllerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }

        // take cards from library and look at them
        boolean topCardRevealed = controller.isTopCardRevealed();
        controller.setTopCardRevealed(false);
        Cards cards = new CardsImpl(controller.getLibrary().getTopCards(game, numberOfCards.calculate(game, source, this)));

        if (revealCards) {
            controller.revealCards(source, cards, game);
        } else {
            controller.lookAtCards(source, null, cards, game);
        }

        boolean result = actionWithLookedCards(game, source, controller, cards);

        controller.setTopCardRevealed(topCardRevealed);

        return result;
    }

    protected boolean actionWithLookedCards(Game game, Ability source, Player player, Cards cards) {
        return moveCards(game, source, player, cards, putLookedCards);
    }

    protected static boolean moveCards(Game game, Ability source, Player player, Cards cards, PutCards putCards) {
        switch (putCards) {
            case TOP_ANY:
                return player.putCardsOnTopOfLibrary(cards, game, source, true);
            case BOTTOM_ANY:
                return player.putCardsOnBottomOfLibrary(cards, game, source, true);
            case BOTTOM_RANDOM:
                return player.putCardsOnBottomOfLibrary(cards, game, source, false);
            case BATTLEFIELD_TAPPED:
                return player.moveCards(cards.getCards(game), Zone.BATTLEFIELD, source, game, true, false, false, null);
            default:
                return player.moveCards(cards, putCards.getZone(), source, game);
        }
    }

    @Override
    public String getText(Mode mode) {
        return setText(mode, "");
    }

    public String setText(Mode mode, String middleText) {
        if (staticText != null && !staticText.isEmpty()) {
            return staticText;
        }
        String numberString = numberOfCards.toString();
        boolean dynamic = !numberOfCards.getMessage().isEmpty();
        boolean oneCard = !dynamic && numberString.equals("1");
        StringBuilder sb = new StringBuilder(revealCards ? "reveal " : "look at ");
        if (oneCard) {
            sb.append("the top card");
        } else if (dynamic) {
            sb.append("the top X cards");
        } else if (numberString.equals("that many")) {
            sb.append("that many cards from the top");
        } else {
            sb.append("the top ").append(CardUtil.numberToText(numberString)).append(" cards");
        }
        sb.append(" of your library");
        if (dynamic) {
            sb.append(", where X is ").append(numberOfCards.getMessage());
        }
        if (!middleText.isEmpty()) {
            sb.append(middleText);
        } else if (!oneCard) {
            sb.append(", then put them ");
            sb.append(putLookedCards == PutCards.TOP_ANY ? "back in any order" : putLookedCards.getMessage(true));
        }
        return sb.toString();
    }
}
