package org.crimsoncrips.craftorio.client.screen.devtools.contract_creator;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.state.ClientContractCreatorDraftState;
import org.crimsoncrips.craftorio.networking.devtools.GenerateContractCodePacket;
import org.crimsoncrips.craftorio.registries.contract.ContractTextColors;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContractItemReward;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public final class ContractCreatorDraft {

    public static final String ID = "contract_id";
    public static final String MOD_ID = "contract_mod_id";
    public static final String SECONDS = "contract_seconds";
    public static final String BASE_VALUE = "contract_base_value";
    public static final String WEIGHT = "contract_weight";
    public static final String CLAIM_THRESHOLD = "contract_claim_threshold";
    public static final String MIN_THRESHOLD = "contract_min_threshold";
    public static final String MAX_THRESHOLD = "contract_max_threshold";
    public static final String PUNISHMENT = "contract_punishment";
    public static final String REQUIRED_MOD = "contract_required_mod";
    public static final String CARD_TEXTURE = "contract_card_texture";
    public static final String JSON_EXPORT = "contract_json_export";
    public static final String TITLE = "contract_title";
    public static final String DESCRIPTION = "contract_description";
    public static final String TITLE_COLOR = "contract_title_color";
    public static final String TIME_COLOR = "contract_time_color";
    public static final String DESCRIPTION_COLOR = "contract_description_color";
    public static final String PUNISHMENT_COLOR = "contract_punishment_color";

    private ContractCreatorDraft() {}

    public static String get(String key) {
        return ClientContractCreatorDraftState.get(key, "");
    }

    public static void set(String key, String value) {
        ClientContractCreatorDraftState.set(key, value);
    }

    public static boolean jsonExport() {
        return get(JSON_EXPORT).equals("true");
    }

    public static void loadIntoDraft(ResourceLocation id, CraftorioContract contract) {
        set(ID, id.getPath());
        set(MOD_ID, id.getNamespace());
        set(SECONDS, String.valueOf(contract.getTime() / CraftorioMisc.SECONDS_TO_TICKS));
        set(BASE_VALUE, CraftorioMisc.toScientificString(contract.getBasePointValue()));
        set(WEIGHT, String.valueOf(contract.getWeight()));
        set(CLAIM_THRESHOLD, CraftorioMisc.toScientificString(contract.getPointThreshold()));
        set(MIN_THRESHOLD, CraftorioMisc.toScientificString(contract.getMinPointThreshold()));
        set(MAX_THRESHOLD, CraftorioMisc.toScientificString(contract.getMaxPointThreshold()));
        set(PUNISHMENT, contract.getPunishment() != null ? contract.getPunishment().toString() : "");
        set(REQUIRED_MOD, contract.getRequiredModId() != null ? contract.getRequiredModId() : "");
        set(CARD_TEXTURE, contract.getCardTexture() != null ? contract.getCardTexture().location().toString() : "");

        String translatedTitle = Component.translatable(contract.getName()).getString();
        String translatedDescription = Component.translatable(contract.getDescription()).getString();
        boolean hasTranslation = !translatedTitle.equals(contract.getName());
        set(TITLE, hasTranslation ? translatedTitle : "");
        set(DESCRIPTION, hasTranslation && !translatedDescription.equals(contract.getDescription()) ? translatedDescription : "");

        ContractTextColors colors = contract.getTextColors();
        set(TITLE_COLOR, hex(colors.title()));
        set(TIME_COLOR, hex(colors.time()));
        set(DESCRIPTION_COLOR, hex(colors.description()));
        set(PUNISHMENT_COLOR, hex(colors.punishment()));

        int rolls = contract.getRewards().stream().mapToInt(CraftorioContractItemReward::getRandomEffectCount).max().orElse(0);
        ContractCreatorRewardScreen.setRewardRollsValue(String.valueOf(rolls));
    }

    private static String hex(Optional<Integer> color) {
        return color.map(ContractTextColors::toHex).orElse("");
    }

    public static void generate() {
        boolean includeLang = !get(TITLE).isBlank() || !get(DESCRIPTION).isBlank();
        PacketDistributor.sendToServer(new GenerateContractCodePacket(
                get(ID),
                get(MOD_ID),
                get(SECONDS),
                get(BASE_VALUE),
                get(WEIGHT),
                get(CLAIM_THRESHOLD),
                get(MIN_THRESHOLD),
                get(MAX_THRESHOLD),
                get(PUNISHMENT),
                get(REQUIRED_MOD),
                ContractCreatorRewardScreen.getRewardRollsValue(),
                includeLang,
                get(TITLE),
                get(DESCRIPTION),
                get(CARD_TEXTURE),
                jsonExport(),
                get(TITLE_COLOR),
                get(TIME_COLOR),
                get(DESCRIPTION_COLOR),
                get(PUNISHMENT_COLOR)
        ));
    }
}
