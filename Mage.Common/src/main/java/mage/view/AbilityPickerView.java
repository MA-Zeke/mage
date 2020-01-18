package mage.view;

import mage.abilities.Ability;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class AbilityPickerView implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<UUID, String> choices = new LinkedHashMap<>();
    private String message = null;

    public AbilityPickerView(String objectName, List<? extends Ability> abilities) {
        for (Ability ability : abilities) {
            if (objectName == null) {
                choices.put(ability.getId(), ability.getRule(true));
            } else {
                String rule = ability.getRule(objectName);
                if (rule.isEmpty()) {
                    rule = ability.toString();
                }
                choices.put(ability.getId(), rule);
            }
        }
    }

    public AbilityPickerView(Map<UUID, String> modes, String message) {
        this.choices = modes;
        this.message = message;
    }

    public Map<UUID, String> getChoices() {
        return choices;
    }

    public String getMessage() {
        return message;
    }
}
