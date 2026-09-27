package org.crimsoncrips.craftorio.client.screen.hub;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.screen.ScrollableScreen;
import org.crimsoncrips.craftorio.client.state.ClientLoanState;
import org.crimsoncrips.craftorio.networking.loan.LoanSharkActionPacket;
import org.crimsoncrips.craftorio.server.loan.CraftorioLoanShark;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class CraftorioLoanSharkScreen extends Screen implements ScrollableScreen {

    private static final int PANEL_WIDTH = 330;
    private static final int PANEL_HEIGHT = 196;
    private static final int PADDING = 10;
    private static final int PORTRAIT_WIDTH = 104;
    private static final long ENTER_MS = 650L;
    private static final long CHAR_MS = 22L;

    private static final char MARK_EMPHASIS = '*';
    private static final char MARK_PAUSE = '~';
    private static final char MARK_PAUSE_ALT = 'º';
    private static final char MARK_BIG = 'þ';
    private static final long PAUSE_EXTRA_MS = 600L;
    private static final long PAUSE_ALT_EXTRA_MS = 200L;
    private static final float BIG_SCALE_PEAK = 2.4f;
    private static final long BIG_SETTLE_MS = 900L;
    private static final float BIG_JITTER_MAGNITUDE = 1.6f;

    private final Screen parent;
    private LivingEntity shark;
    private long openedAt;
    private long dialogueStart;
    private String dialoguePlain = "";
    private boolean[] dialogueEmphasis = new boolean[0];
    private boolean[] dialogueBig = new boolean[0];
    private long[] dialogueRevealAt = new long[0];
    private BigInteger lastOwed;
    private boolean lastHadLoan;

    private int panelLeft;
    private int panelTop;
    private Button takeButton;
    private Button payAllButton;
    private Button payPartialButton;
    private Button leaveButton;

    public CraftorioLoanSharkScreen(Screen parent) {
        super(Component.translatable("misc.craftorio.loan_shark_name"));
        this.parent = parent;
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    private Player player() {
        return this.minecraft.player;
    }

    @Override
    protected void init() {
        this.panelLeft = (this.width - PANEL_WIDTH) / 2;
        this.panelTop = (this.height - PANEL_HEIGHT) / 2;
        if (this.openedAt == 0L) {
            this.openedAt = Util.getMillis();
        }

        if (this.shark == null && this.minecraft.level != null) {
            LivingEntity created = EntityType.VINDICATOR.create(this.minecraft.level);
            if (created != null) {
                created.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_AXE));
                created.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
                this.shark = created;
            }
        }

        int textLeft = this.panelLeft + PORTRAIT_WIDTH + PADDING;
        int buttonWidth = PANEL_WIDTH - PORTRAIT_WIDTH - PADDING * 2;
        int buttonY = this.panelTop + PANEL_HEIGHT - PADDING - 20;

        this.leaveButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
                .bounds(textLeft, buttonY, buttonWidth, 20).build());
        buttonY -= 24;
        this.payPartialButton = this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.loan_shark_pay_partial"), b -> send(CraftorioLoanShark.ACTION_PAY_PARTIAL))
                .bounds(textLeft, buttonY, buttonWidth, 20).build());
        this.takeButton = this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.loan_shark_take"), b -> send(CraftorioLoanShark.ACTION_TAKE))
                .bounds(textLeft, buttonY, buttonWidth, 20).build());
        buttonY -= 24;
        this.payAllButton = this.addRenderableWidget(Button.builder(Component.translatable("misc.craftorio.loan_shark_pay_all"), b -> send(CraftorioLoanShark.ACTION_PAY_ALL))
                .bounds(textLeft, buttonY, buttonWidth, 20).build());

        Player player = player();
        if (player != null) {
            this.lastOwed = CraftorioMisc.getLoanOwed(player);
            this.lastHadLoan = this.lastOwed.signum() > 0;
            speak(introduction(player));
            if (CraftorioMisc.isLoanSacrificed(player)) {
                send(CraftorioLoanShark.ACTION_ACKNOWLEDGE_REMARK);
            }
        }
        refreshButtons();
    }

    private void send(int action) {
        PacketDistributor.sendToServer(new LoanSharkActionPacket(action));
    }

    private void speak(Component line) {
        StringBuilder text = new StringBuilder();
        List<Boolean> emphasis = new ArrayList<>();
        List<Boolean> big = new ArrayList<>();
        List<Long> pauseAfter = new ArrayList<>();
        boolean emphasized = false;
        boolean bigWord = false;

        for (char c : line.getString().toCharArray()) {
            if (c == MARK_EMPHASIS) {
                emphasized = !emphasized;
                continue;
            }
            if (c == MARK_PAUSE || c == MARK_PAUSE_ALT) {
                if (!pauseAfter.isEmpty()) {
                    long extra = c == MARK_PAUSE ? PAUSE_EXTRA_MS : PAUSE_ALT_EXTRA_MS;
                    pauseAfter.set(pauseAfter.size() - 1, pauseAfter.get(pauseAfter.size() - 1) + extra);
                }
                continue;
            }
            if (c == MARK_BIG) {
                bigWord = true;
                continue;
            }
            if (Character.isWhitespace(c)) {
                bigWord = false;
            }

            text.append(c);
            emphasis.add(emphasized);
            big.add(bigWord);
            pauseAfter.add(0L);
        }

        this.dialoguePlain = text.toString();
        this.dialogueEmphasis = new boolean[emphasis.size()];
        this.dialogueBig = new boolean[big.size()];
        this.dialogueRevealAt = new long[emphasis.size()];
        long time = 0L;
        for (int i = 0; i < emphasis.size(); i++) {
            this.dialogueEmphasis[i] = emphasis.get(i);
            this.dialogueBig[i] = big.get(i);
            time += CHAR_MS;
            this.dialogueRevealAt[i] = time;
            time += pauseAfter.get(i);
        }

        this.dialogueStart = Util.getMillis();
    }

    private long dialogueTotalMs() {
        return this.dialogueRevealAt.length == 0 ? 0L : this.dialogueRevealAt[this.dialogueRevealAt.length - 1];
    }

    private int dialogueShownChars() {
        long elapsed = Util.getMillis() - this.dialogueStart;
        int shown = 0;
        while (shown < this.dialogueRevealAt.length && this.dialogueRevealAt[shown] <= elapsed) {
            shown++;
        }
        return shown;
    }

    private Component styledRun(int start, int end) {
        Component run = Component.literal(this.dialoguePlain.substring(start, end));
        return this.dialogueEmphasis[start] ? run.copy().withStyle(ChatFormatting.RED, ChatFormatting.BOLD) : run;
    }

    private Component rangeComponent(int start, int end) {
        MutableComponent result = Component.empty();
        int i = start;
        while (i < end) {
            boolean emphasized = this.dialogueEmphasis[i];
            int runStart = i;
            while (i < end && this.dialogueEmphasis[i] == emphasized) {
                i++;
            }
            result.append(styledRun(runStart, i));
        }
        return result;
    }

    private int measureWidth(int start, int end) {
        return this.font.width(rangeComponent(start, end));
    }

    private List<int[]> wrapLines(int shown, int maxWidth) {
        List<int[]> lines = new ArrayList<>();
        int start = 0;
        while (start < shown) {
            int end = start;
            int lastBreak = -1;
            while (end < shown && measureWidth(start, end + 1) <= maxWidth) {
                if (this.dialoguePlain.charAt(end) == ' ') lastBreak = end + 1;
                end++;
            }
            if (end >= shown) {
                lines.add(new int[]{start, shown});
                break;
            }
            int breakAt = lastBreak > start ? lastBreak : Math.max(end, start + 1);
            lines.add(new int[]{start, breakAt});
            start = breakAt;
            while (start < shown && this.dialoguePlain.charAt(start) == ' ') {
                start++;
            }
        }
        if (lines.isEmpty()) {
            lines.add(new int[]{0, 0});
        }
        return lines;
    }

    private Component introduction(Player player) {
        BigInteger points = CraftorioMisc.getPoints(player);
        BigInteger owed = CraftorioMisc.getLoanOwed(player);
        BigInteger borrowed = CraftorioMisc.getLoanBorrowed(player);
        double interest = ClientLoanState.getInterestPercent();

        if (owed.signum() > 0) {
            if (CraftorioMisc.isLoanSacrificed(player)) {
                return Component.translatable("misc.craftorio.loan_shark_sacrificed", CraftorioMisc.bigIntFormat(owed));
            }
            Component base = Component.translatable("misc.craftorio.loan_shark_debt", CraftorioMisc.bigIntFormat(owed), CraftorioMisc.bigIntFormat(borrowed), trim(interest));
            return points.signum() < 0 ? base.copy().append(" ").append(Component.translatable("misc.craftorio.loan_shark_debt_negative")) : base;
        }
        if (points.signum() < 0) {
            BigInteger deficit = points.negate();
            return Component.translatable("misc.craftorio.loan_shark_offer", CraftorioMisc.bigIntFormat(deficit),
                    CraftorioMisc.bigIntFormat(CraftorioLoanShark.withInterest(deficit, interest)), trim(interest));
        }
        return Component.translatable("misc.craftorio.loan_shark_nothing");
    }

    private static String trim(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private void refreshButtons() {
        Player player = player();
        if (player == null) return;

        BigInteger points = CraftorioMisc.getPoints(player);
        BigInteger owed = CraftorioMisc.getLoanOwed(player);
        boolean loan = owed.signum() > 0;
        boolean offer = !loan && points.signum() < 0;

        this.takeButton.visible = offer;
        this.takeButton.active = offer;
        this.payAllButton.visible = loan;
        this.payAllButton.active = loan && points.compareTo(owed) >= 0;
        this.payPartialButton.visible = loan;
        this.payPartialButton.active = loan && points.signum() > 0;
    }

    @Override
    public void tick() {
        Player player = player();
        if (player == null) return;

        BigInteger owed = CraftorioMisc.getLoanOwed(player);
        boolean loan = owed.signum() > 0;
        if (loan && !this.lastHadLoan) {
            speak(Component.translatable("misc.craftorio.loan_shark_accepted", CraftorioMisc.bigIntFormat(owed)));
        } else if (!loan && this.lastHadLoan) {
            speak(Component.translatable("misc.craftorio.loan_shark_paid_full"));
        } else if (loan && this.lastOwed != null && owed.compareTo(this.lastOwed) < 0) {
            speak(Component.translatable("misc.craftorio.loan_shark_paid_partial", CraftorioMisc.bigIntFormat(owed)));
        }
        this.lastOwed = owed;
        this.lastHadLoan = loan;
        refreshButtons();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        if (dialogueShownChars() < this.dialoguePlain.length()) {
            this.dialogueStart = Util.getMillis() - dialogueTotalMs();
            return true;
        }
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(this.panelLeft, this.panelTop, this.panelLeft + PANEL_WIDTH, this.panelTop + PANEL_HEIGHT, 0xE01A0F0F);
        graphics.renderOutline(this.panelLeft, this.panelTop, PANEL_WIDTH, PANEL_HEIGHT, 0xFF8B1A1A);
        graphics.drawCenteredString(this.font, this.title, this.panelLeft + PANEL_WIDTH / 2, this.panelTop + 6, 0xFF5555);

        renderShark(graphics, mouseX, mouseY);
        renderDialogue(graphics, dialogueShownChars());

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderShark(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.shark == null) return;

        float t = Mth.clamp((Util.getMillis() - this.openedAt) / (float) ENTER_MS, 0f, 1f);
        float eased = 1f - (1f - t) * (1f - t) * (1f - t);
        int slide = Math.round((1f - eased) * (PANEL_WIDTH - PORTRAIT_WIDTH));

        int x1 = this.panelLeft + 4 + slide;
        int x2 = this.panelLeft + PORTRAIT_WIDTH - 4 + slide;
        int y1 = this.panelTop + 20;
        int y2 = this.panelTop + PANEL_HEIGHT - 8;

        graphics.enableScissor(this.panelLeft + 2, this.panelTop + 2, this.panelLeft + PORTRAIT_WIDTH, this.panelTop + PANEL_HEIGHT - 2);
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, x1, y1, x2, y2, 52, 0.0625F, mouseX, mouseY, this.shark);
        graphics.disableScissor();
    }

    private void renderDialogue(GuiGraphics graphics, int shown) {
        int textLeft = this.panelLeft + PORTRAIT_WIDTH + PADDING;
        int textWidth = PANEL_WIDTH - PORTRAIT_WIDTH - PADDING * 2;

        long now = Util.getMillis();
        int y = this.panelTop + 22;
        for (int[] range : wrapLines(shown, textWidth)) {
            drawLine(graphics, textLeft, y, range[0], range[1], now);
            y += this.font.lineHeight + 1;
        }
    }

    private void drawLine(GuiGraphics graphics, int left, int y, int start, int end, long now) {
        int x = left;
        int i = start;
        while (i < end) {
            if (this.dialogueBig[i]) {
                x = drawBigChar(graphics, x, y, i, now);
                i++;
                continue;
            }
            int runStart = i;
            boolean emphasized = this.dialogueEmphasis[i];
            while (i < end && !this.dialogueBig[i] && this.dialogueEmphasis[i] == emphasized) {
                i++;
            }
            Component run = styledRun(runStart, i);
            graphics.drawString(this.font, run, x, y, 0xE8D8D8, false);
            x += this.font.width(run);
        }
    }

    private int drawBigChar(GuiGraphics graphics, int x, int y, int index, long now) {
        Component character = styledRun(index, index + 1);
        int advance = this.font.width(character);

        long age = now - this.dialogueStart - this.dialogueRevealAt[index];
        float decay = Mth.clamp(1f - age / (float) BIG_SETTLE_MS, 0f, 1f);
        decay *= decay;
        float scale = 1f + (BIG_SCALE_PEAK - 1f) * decay;
        float jitterX = (float) Math.sin((now + index * 137L) * 0.03) * BIG_JITTER_MAGNITUDE * decay;
        float jitterY = (float) Math.cos((now + index * 211L) * 0.037) * BIG_JITTER_MAGNITUDE * decay;

        float pivotX = x + advance / 2f + jitterX;
        float pivotY = y + this.font.lineHeight / 2f + jitterY;

        graphics.pose().pushPose();
        graphics.pose().translate(pivotX, pivotY, 0f);
        graphics.pose().scale(scale, scale, 1f);
        graphics.drawString(this.font, character, Math.round(-advance / 2f), Math.round(-this.font.lineHeight / 2f), 0xE8D8D8, false);
        graphics.pose().popPose();

        return x + advance;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
