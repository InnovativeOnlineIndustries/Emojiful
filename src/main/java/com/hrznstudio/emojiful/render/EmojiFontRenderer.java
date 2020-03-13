package com.hrznstudio.emojiful.render;

import com.hrznstudio.emojiful.Emojiful;
import com.hrznstudio.emojiful.EmojifulConfig;
import com.hrznstudio.emojiful.api.Emoji;
import io.netty.util.internal.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.regex.Pattern;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;

public class EmojiFontRenderer extends FontRenderer {
    private Map<Integer, Emoji> emojis = new HashMap<>();

    public EmojiFontRenderer(Minecraft minecraft) {
        super(minecraft.gameSettings, new ResourceLocation("textures/font/ascii.png"), minecraft.renderEngine, false);
        super.onResourceManagerReload(minecraft.getResourceManager());
    }

    @Override
    public int getStringWidth(String text) {
        text = getEmojiFormattedString(text);
        return super.getStringWidth(text);
    }

    private String getEmojiFormattedString(String text) {
        if (EmojifulConfig.renderEmoji && !StringUtil.isNullOrEmpty(text)) {
            String unformattedText = TextFormatting.getTextWithoutFormattingCodes(text);
            if (StringUtil.isNullOrEmpty(unformattedText))
                return text;
            String[] split = unformattedText.split(" ");
            List<Pair<Emoji, String>> addedEmojis = new ArrayList<>();
            for (String word : split) {
                Emoji wordEmoji = null;
                for (Emoji emoji : Emojiful.EMOJI_LIST) {
                    if (emoji.test(word.trim())) {
                        wordEmoji = emoji;
                        break;
                    }
                }
                if (wordEmoji != null) {
                    addedEmojis.add(Pair.of(wordEmoji, word));
                }
            }
            String fomattingText = text.toLowerCase(Locale.ENGLISH);
            for (Pair<Emoji, String> entry : addedEmojis) {
                String emojiText = entry.getValue().toLowerCase(Locale.ENGLISH);
                int index = fomattingText.indexOf(emojiText);
                this.emojis.put(index, entry.getKey());
                fomattingText = fomattingText.replaceFirst(Pattern.quote(emojiText), "\u2603");
                text = text.replaceFirst("(?i)" + Pattern.quote(emojiText), "\u2603");
            }
        }
        return text;
    }

    @Override
    public int getCharWidth(char character) {
        if (character == '\u2603')
            return 10;
        return super.getCharWidth(character);
    }

    @Override
    protected void renderStringAtPos(String text, boolean hasShadow) {
        if (text.isEmpty())
            return;
        this.emojis.clear();
        text = getEmojiFormattedString(text);
        for (int charIndex = 0; charIndex < text.length(); ++charIndex) {
            char character = text.charAt(charIndex);
            if (character == 167 && charIndex + 1 < text.length()) {
                int formatting = "0123456789abcdefklmnor".indexOf(text.toLowerCase(Locale.ENGLISH).charAt(charIndex + 1));
                if (formatting < 16) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    if (formatting < 0 || formatting > 15) {
                        formatting = 15;
                    }
                    if (hasShadow) {
                        formatting += 16;
                    }
                    int colour = this.colorCode[formatting];
                    this.textColor = colour;
                    setColor((colour >> 16) / 255.0F, (colour >> 8 & 255) / 255.0F, (colour & 255) / 255.0F, this.alpha);
                } else if (formatting == 16) {
                    this.randomStyle = true;
                } else if (formatting == 17) {
                    this.boldStyle = true;
                } else if (formatting == 18) {
                    this.strikethroughStyle = true;
                } else if (formatting == 19) {
                    this.underlineStyle = true;
                } else if (formatting == 20) {
                    this.italicStyle = true;
                } else if (formatting == 21) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    setColor(this.red, this.blue, this.green, this.alpha);
                }
                ++charIndex;
            } else {
                int c = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(character);
                if (this.randomStyle && c != -1) {
                    int width = this.getCharWidth(character);
                    char newChar;
                    while (true) {
                        c = this.fontRandom.nextInt("\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".length());
                        newChar = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".charAt(c);
                        if (width == this.getCharWidth(newChar)) {
                            break;
                        }
                    }
                    character = newChar;
                }
                float size = c == -1 || this.unicodeFlag ? 0.5F : 1.0F;
                boolean shadow = (character == 0 || c == -1 || this.unicodeFlag) && hasShadow;
                if (shadow) {
                    this.posX -= size;
                    this.posY -= size;
                }
                float offset = this.renderChar(character, this.italicStyle, charIndex);
                if (shadow) {
                    this.posX += size;
                    this.posY += size;
                }
                if (this.boldStyle) {
                    this.posX += size;
                    if (shadow) {
                        this.posX -= size;
                        this.posY -= size;
                    }
                    this.renderChar(character, this.italicStyle, charIndex);
                    this.posX -= size;
                    if (shadow) {
                        this.posX += size;
                        this.posY += size;
                    }
                    ++offset;
                }
                doDraw(offset);
            }
        }
    }

    private float renderChar(char c, boolean italic, int index) {
        if (EmojifulConfig.renderEmoji) {
            Emoji emoji = this.emojis.get(index);
            if (emoji != null) {
                bindTexture(emoji.getResourceLocationForBinding());
                return this.renderEmoji(emoji);
            }
        }
        if (c == ' ') {
            return 4.0F;
        } else {
            int charIndex = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(c);
            return charIndex != -1 && !this.unicodeFlag ? this.renderDefaultChar(charIndex, italic) : this.renderUnicodeChar(c, italic);
        }
    }

    private float renderEmoji(Emoji emoji) {
        float textureSize = 16;
        float textureX = 0 / textureSize;
        float textureY = 0 / textureSize;
        float textureOffset = 16.0F / textureSize;
        float size = 10f;
        float offsetY = 1.0F;
        float offsetX = 0.0F;
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();

        GlStateManager.color(1, 1, 1);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GlStateManager.glBegin(GL11.GL_TRIANGLE_STRIP);
        GlStateManager.glTexCoord2f(textureX, textureY);
        GlStateManager.glVertex3f(this.posX - offsetX, this.posY - offsetY, 0.0F);
        GlStateManager.glTexCoord2f(textureX, textureY + textureOffset);
        GlStateManager.glVertex3f(this.posX - offsetX, this.posY + size - offsetY, 0.0F);
        GlStateManager.glTexCoord2f(textureX + textureOffset, textureY / textureSize);
        GlStateManager.glVertex3f(this.posX - offsetX + size, this.posY - offsetY, 0.0F);
        GlStateManager.glTexCoord2f(textureX + textureOffset, textureY + textureOffset);
        GlStateManager.glVertex3f(this.posX - offsetX + size, this.posY + size - offsetY, 0.0F);
        GlStateManager.glEnd();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        GlStateManager.disableBlend();
        setColor(this.red, this.green, this.blue, this.alpha);
        return 10f;
    }
}
