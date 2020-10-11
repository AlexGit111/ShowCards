import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ShowCards {

    public static void main(String[] args) throws IOException {
        JFrame frame = buildFrame();

        final BufferedImage image = ImageIO.read(new File(args[0]));
        ArrayList<BufferedImage> symbPicArr = new ArrayList<BufferedImage>();
        symbPicArr.add(ImageIO.read(new File(".\\SYMBOLS\\symbA.png")));
        symbPicArr.add(ImageIO.read(new File(".\\SYMBOLS\\symb2.png")));
        symbPicArr.add(ImageIO.read(new File(".\\SYMBOLS\\symb3.png")));
        symbPicArr.add(ImageIO.read(new File(".\\SYMBOLS\\symb4.png")));
        symbPicArr.add(ImageIO.read(new File(".\\SYMBOLS\\symb5.png")));
        symbPicArr.add(ImageIO.read(new File(".\\SYMBOLS\\symb6.png")));
        symbPicArr.add(ImageIO.read(new File(".\\SYMBOLS\\symb7.png")));
        symbPicArr.add(ImageIO.read(new File(".\\SYMBOLS\\symb8.png")));
        symbPicArr.add(ImageIO.read(new File(".\\SYMBOLS\\symb9.png")));
        symbPicArr.add(ImageIO.read(new File(".\\SYMBOLS\\symb10.png")));
        symbPicArr.add(ImageIO.read(new File(".\\SYMBOLS\\symbK.png")));
        symbPicArr.add(ImageIO.read(new File(".\\SYMBOLS\\symbQ.png")));
        symbPicArr.add(ImageIO.read(new File(".\\SYMBOLS\\symbJ.png")));
        String [] symbLetterArr = new String[] {"A","2","3","4","5","6","7","8","9","10","K","Q","J"};


        int width = image.getWidth();
        int height = image.getHeight();

        Map<Integer, Integer> cardSymbDict = new HashMap<>();

        boolean foundBegin = false; //найден верхний левый участок карточки
        int whitePxlCnt = 0;
        final int minWhitePxls = 25;
        final int whiteDiff = 100;
        ArrayList<int[]> cardRects = new ArrayList<int[]>();
        int [] cardRect = null;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                //Color clr1 = new Color( image.getRGB(x, y));
                int whiteClr = Color.white.getRGB();
                if(image.getRGB(x, y) == whiteClr) {
                //if(clrDistanceToWhite(image.getRGB(x, y)) < whiteDiff) {
                    whitePxlCnt++;//плюсуем количество белых пикселей идущих подряд
                    if(!foundBegin && whitePxlCnt >= minWhitePxls) { //если количество белых пикселей больше предела
                        cardRects.add(new int[] {0,0,0,0});
                        cardRect = cardRects.get(cardRects.size()-1);
                        cardRect[0] = x;
                        cardRect[1] = y - minWhitePxls + 1 ;

                        //поиск высоты карты
                        int h;
                        for( h = cardRect[1]; h < height && clrDistanceToWhite(image.getRGB(cardRect[0], h)) < whiteDiff; h++)
                            cardRect[3]++;
                        //System.out.println(clrDistanceToWhite(image.getRGB(x, h)));

                        //поиск ширины карты
                        int w;
                        for(w = cardRect[0]; w < width && clrDistanceToWhite(image.getRGB(w, cardRect[1])) < whiteDiff ; w++) cardRect[2]++;
                        //System.out.println(clrDistanceToWhite(image.getRGB(w, y)));

                        x += cardRect[2] ; //добавляем к текущей координате x длину карты, чтобы не искать белые полосы в найденном прямоугольнике

                        whitePxlCnt = 0;
                    }

                }
                else //следующий пиксель не белый
                {
                    if(whitePxlCnt < minWhitePxls) whitePxlCnt = 0; //если количество белых пикселей меньше нужного, обнуляем и ищем следующий участок
                }
            }
        }

        ArrayList<int[]> symbRects = new ArrayList<int[]>();
        //для всех найденных карт(белых прямоугольгольных областей), ищем внутри карточные знаки
        int cardInxd = 0;
        int arrPrevIndx = 0;

        for(int d = 0; d < cardRects.size(); d++) {
            int [] rect = cardRects.get(d);
            if (rect[2] < 40 || rect[3] < 40) { //если прямоугольный регион карты слишком малый, пропускаем
                cardRects.remove(cardRects.get(d));
            }
        }

        for(int [] rect : cardRects) {
            cardInxd++;

            int x1 = rect[0];
            int y1 = rect[1];
            int x2 = rect[0] + rect[2];
            int y2 = rect[1] + rect[3];

            //поиск координат самого верхнего пикселя
            int prevFirstIndx = -1;
            int pxlFound = 0; //статус поиска НЕбелых пиксей
            int leftX = x2; //самый левый пиксель текущего региона
            for (int y = y1; y < y2; y++) {//сканируем горизонтальные строки
                if(pxlFound == 1) pxlFound = 2; //если найден верхний пиксель, искать белую линию
                int x;
                for (x = x1; x < x2; x++) {
                    if(clrDistanceToWhite(image.getRGB(x,y)) > 100) {  //в сканируемой горизонтальной линии найден самый верхний НЕбелый пиксель
                        if(pxlFound > 0) { //если верхний НЕбелый пиксель найден, искать белую линию(нижнюю черту региона)
                            pxlFound = 1; //в этой линии найден не белый пиксель, не считаем эту линию за нижнюю черту
                            break;
                        }
                        else { //если новый поиск и найден НЕбелый пиксель
                            symbRects.add(new int[]{x, y, 34, 1});
                            leftX = x;
                            pxlFound = 1; //найден верхний НЕбелый пиксель, но не найдена белая линия
                        }
                    }
                }
                if (leftX > x) leftX = x;
                if(pxlFound == 2) { //если верхний пиксель найден, проверяем найдена ли полностью белая строка
                    //System.out.println(y);
                    pxlFound = 0; //новый поиск верхнего пикселя
                    symbRects.get(symbRects.size()-1)[3] = y - y1;
                    symbRects.get(symbRects.size()-1)[0] = leftX;
                    if(prevFirstIndx == -1) prevFirstIndx = symbRects.size();
                }


                if(prevFirstIndx != -1) { //для самого первого найденного региона сравнение с множеством символов
                    int[] sRect = symbRects.get(prevFirstIndx-1); // первый регион на карточке (буква в самом верху)
                    ///////////////////////////////сравнение символов с картами///////////////////////////////////////////

                    int [] diffsArr = new int[symbPicArr.size()];
                    int arrImgIdx = 0;
                    for(BufferedImage arrImg : symbPicArr) {
                        diffsArr[arrImgIdx] = 0;
                        int arrImgW = arrImg.getWidth();
                        int arrImgH = arrImg.getHeight();

                        //Compare
                        for (int xC = 0; xC < arrImgW; xC++) {
                            for (int yC = 0; yC < arrImgH; yC++) {
                                boolean arrImgPxlWhite = clrDistanceToWhite(arrImg.getRGB(xC, yC)) < whiteDiff;
                                boolean imagePxlWhite = clrDistanceToWhite(image.getRGB(sRect[0] + xC, sRect[1] + yC)) < whiteDiff;
                                if(arrImgPxlWhite != imagePxlWhite) { //если на примере символа и на картинке не совпадают пиксели
                                    diffsArr[arrImgIdx] += 1; //увеличиваем количество несовпадений для данного примера(его индекса)
                                }
                                //else matchArr[arrImgIdx] -= 1;
                            }
                        }//конец цикла сравнения с одним из символов
                        arrImgIdx++;
                    } //конец цикла сравнения со всеми символами из всех возможных
                    int minDiff = diffsArr[0];
                    int minDiffIdx = 0;
                    for (int dIdx = 1; dIdx < diffsArr.length; dIdx++) if(diffsArr[dIdx] < minDiff) {
                        if(minDiff > diffsArr[dIdx]) minDiff = diffsArr[dIdx];
                        minDiffIdx = dIdx;
                    }
                    if (minDiff < 50) {
                        cardSymbDict.put(cardInxd, minDiffIdx);
                        //System.out.println(cardInxd + " -> " + symbLetterArr[diffIdx]);
                    }
                }
            }
        }
        for (Integer key : cardSymbDict.keySet()) {
            System.out.print("card" + key + "->[" + symbLetterArr[cardSymbDict.get(key)] + "] ");
        }
		System.out.println("");




        JPanel pane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, null);
                g.setColor(Color.red);
				g.setFont(new Font("SansSerif", Font.BOLD, 15));
                int crdIdx = 1;
                for(int[] rect : cardRects) {
					
                    if(cardSymbDict.containsKey(crdIdx) ) g.drawString(symbLetterArr[cardSymbDict.get(crdIdx)], rect[0], rect[1] - 8);
                    g.drawRect(rect[0], rect[1], rect[2], rect[3]);
                    crdIdx++;
                }

                g.setColor(Color.blue);
                for(int[] rect : symbRects) g.drawRect(rect[0], rect[1], rect[2], rect[3]);

            }
        };


        frame.add(pane);
        //frame.setLocationRelativeTo(null);
        frame.setSize(width + 50, height + 50);
        frame.setVisible(true);
    }


    public static int clrDistanceToWhite(int clrInt) {
        Color clr = new Color(clrInt);
        int clrSum = clr.getBlue() + clr.getRed() + clr.getGreen();
        return ((255+255+255) - clrSum);
    }


    private static JFrame buildFrame() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        return frame;
    }


}
