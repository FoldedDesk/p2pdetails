package com.danzer.p2pdetails;

import java.io.IOException;
import java.util.Locale;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class GuiP2PConfigTool extends GuiScreen {

    private GuiTextField input;
    private GuiButton saveButton;
    private GuiButton cancelButton;
    private String errorText = "";

    @Override
    public void initGui() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.input = new GuiTextField(0, this.fontRenderer, centerX - 100, centerY - 20, 200, 20);
        this.input.setMaxStringLength(10);
        this.input.setFocused(true);

        this.buttonList.clear();
        this.saveButton = new GuiButton(1, centerX - 100, centerY + 10, 96, 20, "Save");
        this.cancelButton = new GuiButton(2, centerX + 4, centerY + 10, 96, 20, "Cancel");
        this.buttonList.add(this.saveButton);
        this.buttonList.add(this.cancelButton);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.input.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        drawCenteredString(this.fontRenderer, "P2P Config Tool", centerX, centerY - 60, 0xFFFFFF);
        drawCenteredString(this.fontRenderer, "Frequency:", centerX, centerY - 40, 0xC0C0C0);
        this.input.drawTextBox();
        if (!this.errorText.isEmpty()) {
            drawCenteredString(this.fontRenderer, this.errorText, centerX, centerY + 36, 0xFF5555);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 2) {
            this.mc.displayGuiScreen(null);
            return;
        }
        if (button.id == 1) {
            Integer parsed = parseFrequency(this.input.getText());
            if (parsed == null) {
                this.errorText = "Invalid value";
                return;
            }
            P2PDetailsMod.NETWORK.sendToServer(new PacketSetNextFrequency(parsed.intValue()));
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            this.mc.displayGuiScreen(null);
            return;
        }
        if (keyCode == 28 || keyCode == 156) {
            actionPerformed(this.saveButton);
            return;
        }
        if (this.input.textboxKeyTyped(typedChar, keyCode)) {
            this.errorText = "";
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.input.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private Integer parseFrequency(String raw) {
        String text = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        if (text.isEmpty()) {
            return null;
        }
        try {
            int value;
            if (text.startsWith("0x")) {
                value = Integer.parseInt(text.substring(2), 16);
            } else if (text.matches(".*[a-f].*")) {
                value = Integer.parseInt(text, 16);
            } else {
                value = Integer.parseInt(text, 10);
            }
            return (value >= 1 && value <= 65535) ? Integer.valueOf(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
