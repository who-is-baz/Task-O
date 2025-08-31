import javax.swing.*;
import java.awt.*;

/** Viewport que dibuja una imagen de fondo estilo "cover" con overlay opcional. */
public class ImageViewport extends JViewport {
    private Image backgroundImage;
    private float overlayDark = 0.0f; // 0 = sin overlay; 0.12f recomendado

    public void setBackgroundImage(Image img) { this.backgroundImage = img; repaint(); }
    public void setOverlayDark(float v) {
        this.overlayDark = Math.max(0f, Math.min(1f, v));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth(), h = getHeight();
        int iw = backgroundImage.getWidth(this), ih = backgroundImage.getHeight(this);
        if (iw > 0 && ih > 0) {
            double scale = Math.max(w / (double) iw, h / (double) ih); // COVER
            int dw = (int) (iw * scale), dh = (int) (ih * scale);
            int x = (w - dw) / 2, y = (h - dh) / 2;
            g2.drawImage(backgroundImage, x, y, dw, dh, this);
        }
        if (overlayDark > 0f) {
            g2.setComposite(AlphaComposite.SrcOver.derive(overlayDark));
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, w, h);
        }
        g2.dispose();
    }
}
