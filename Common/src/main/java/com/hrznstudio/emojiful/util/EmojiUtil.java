package com.hrznstudio.emojiful.util;

import com.hrznstudio.emojiful.api.Emoji;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class EmojiUtil {

    private EmojiUtil() {
    }

    public static float renderEmoji(Emoji emoji, float x, float y, Matrix4f matrix, MultiBufferSource buffer, int packedLight) {
        float textureSize = 16;
        float textureX = 0 / textureSize;
        float textureY = 0 / textureSize;
        float textureOffset = 16.0F / textureSize;
        float size = 10f;
        float offsetY = 1.0F;
        float offsetX = 0.0F;

        VertexConsumer builder = buffer.getBuffer(RenderTypes.text(emoji.getResourceLocationForBinding()));

        builder.addVertex(matrix, x - offsetX, y - offsetY, 0.0f).setColor(255, 255, 255, 255).setUv(textureX, textureY).setLight(packedLight);
        builder.addVertex(matrix, x - offsetX, y + size - offsetY, 0.0F).setColor(255, 255, 255, 255).setUv(textureX, textureY + textureOffset).setLight(packedLight);
        builder.addVertex(matrix, x - offsetX + size, y + size - offsetY, 0.0F).setColor(255, 255, 255, 255).setUv(textureX + textureOffset, textureY + textureOffset).setLight(packedLight);
        builder.addVertex(matrix, x - offsetX + size, y - offsetY, 0.0F).setColor(255, 255, 255, 255).setUv(textureX + textureOffset, textureY / textureSize).setLight(packedLight);

        return 10f;
    }

    public static String cleanStringForRegex(String string) {
        return string.replaceAll("\\)", "\\\\)").replaceAll("\\(", "\\\\(").replaceAll("\\|", "\\\\|").replaceAll("\\*", "\\\\*");
    }

    public static List<Pair<BufferedImage, Integer>> splitGif(File file) throws IOException {
        List<Pair<BufferedImage, Integer>> images = new ArrayList<>();
        ImageReader reader = ImageIO.getImageReadersBySuffix("gif").next();
        reader.setInput(ImageIO.createImageInputStream(new FileInputStream(file)), false);
        IIOMetadata metadata = reader.getImageMetadata(0);
        String metaFormatName = metadata.getNativeMetadataFormatName();
        for (int i = 0; i < reader.getNumImages(true); i++) {
            int frameLength = 1;
            BufferedImage image = reader.read(i);
            BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
            newImage.getGraphics().drawImage(image, 0, 0, null);
            IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(i).getAsTree(metaFormatName);
            // Find GraphicControlExtension node
            int nNodes = root.getLength();
            for (int j = 0; j < nNodes; j++) {
                Node node = root.item(j);
                if (node.getNodeName().equalsIgnoreCase("GraphicControlExtension")) {
                    // Get delay value
                    frameLength = Integer.parseInt(((IIOMetadataNode) node).getAttribute("delayTime"));
                    // Check if delay is bugged
                    break;
                }
            }
            images.add(Pair.of(newImage, frameLength));
        }
        return images;
    }

}
