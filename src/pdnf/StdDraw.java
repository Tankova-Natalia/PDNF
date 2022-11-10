/*************************************************************************
 *  Компиляция:  javac StdDraw.java
 *  Выполнение:    java StdDraw
 *
 *  Библиотека рисования. Этот класс обеспечивает основные возмонжости  для 
 *  создания изображений в вашей программе. Она использует простую модель, 
 *  которая позволяет вам создавать изображения, состоящие из точек, линий, 
 *  кривых, а также сохранять изображение в файл.
 *************************************************************************/

package pdnf;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class StdDraw implements ActionListener, MouseListener, MouseMotionListener, KeyListener {

    // предопределенные цвета
    public static final Color BLACK      = Color.BLACK;
    public static final Color BLUE       = Color.BLUE;
    public static final Color CYAN       = Color.CYAN;
    public static final Color DARK_GRAY  = Color.DARK_GRAY;
    public static final Color GRAY       = Color.GRAY;
    public static final Color GREEN      = Color.GREEN;
    public static final Color LIGHT_GRAY = Color.LIGHT_GRAY;
    public static final Color MAGENTA    = Color.MAGENTA;
    public static final Color ORANGE     = Color.ORANGE;
    public static final Color PINK       = Color.PINK;
    public static final Color RED        = Color.RED;
    public static final Color WHITE      = Color.WHITE;
    public static final Color YELLOW     = Color.YELLOW;

    public static final Color BOOK_BLUE       = new Color(  9,  90, 166);
    public static final Color BOOK_LIGHT_BLUE = new Color(103, 198, 243);

    public static final Color BOOK_RED = new Color(150, 35, 31);

    // Цвета по умолчанию
    private static final Color DEFAULT_PEN_COLOR   = BLACK;
    private static final Color DEFAULT_CLEAR_COLOR = WHITE;

    // Текущий цвет
    private static Color penColor;

    // Размер холста по умолчанию DEFAULT_SIZE-by-DEFAULT_SIZE
    private static final int DEFAULT_SIZE = 512;
    private static int width  = DEFAULT_SIZE;
    private static int height = DEFAULT_SIZE;

    // Размер карандаша по умолчанию
    private static final double DEFAULT_PEN_RADIUS = 0.002;

    // Текущий размер карандаша
    private static double penRadius;

    // Показывать изображение немедленно или подождать до следующего вызова show?
    private static boolean defer = false;

    // ограничения для холста, 5% граница
    private static final double BORDER = 0.05;
    private static final double DEFAULT_XMIN = 0.0;
    private static final double DEFAULT_XMAX = 1.0;
    private static final double DEFAULT_YMIN = 0.0;
    private static final double DEFAULT_YMAX = 1.0;
    private static double xmin, ymin, xmax, ymax;

    // для синхронизации
    private static Object mouseLock = new Object();
    private static Object keyLock = new Object();

    // шрифт по умолчанию
    private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 16);

    // текущий шрифт
    private static Font font;

    // двойная буферизация графики
    private static BufferedImage offscreenImage, onscreenImage;
    private static Graphics2D offscreen, onscreen;

    // для обратных вызовов
    private static StdDraw std = new StdDraw();

    // фрейм для рисования на экране
    private static JFrame frame;

    // состояние мыши
    private static boolean mousePressed = false;
    private static double mouseX = 0;
    private static double mouseY = 0;

    // очередь напечатанных символов
    private static LinkedList<Character> keysTyped = new LinkedList<Character>();

    // множество кодов нажатых клавиш
    private static TreeSet<Integer> keysDown = new TreeSet<Integer>();
  

    // 
    private StdDraw() { }


    // статическая инициализация
    static { init(); }

    /**
     * Устанавливает размер окна 512 на 512 пикселей.
     */
    public static void setCanvasSize() {
        setCanvasSize(DEFAULT_SIZE, DEFAULT_SIZE);
    }

    /**
     * Устанавливает размер окна w на h пикселей.
     *
     * @param w ширина окна, число пикселей
     * @param h высота окна, число пикселей
     * @throws RunTimeException, если высота или ширина 0 или отрицательное
     */
    public static void setCanvasSize(int w, int h) {
        if (w < 1 || h < 1) throw new RuntimeException("width and height must be positive");
        width = w;
        height = h;
        init();
    }

    // инициализация
    private static void init() {
        if (frame != null) frame.setVisible(false);
        frame = new JFrame();
        offscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        onscreenImage  = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        offscreen = offscreenImage.createGraphics();
        onscreen  = onscreenImage.createGraphics();
        setXscale();
        setYscale();
        offscreen.setColor(DEFAULT_CLEAR_COLOR);
        offscreen.fillRect(0, 0, width, height);
        setPenColor();
        setPenRadius();
        setFont();
        clear();

        // add antialiasing
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                                                  RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        offscreen.addRenderingHints(hints);

        // frame stuff
        ImageIcon icon = new ImageIcon(onscreenImage);
        JLabel draw = new JLabel(icon);

        draw.addMouseListener(std);
        draw.addMouseMotionListener(std);

        frame.setContentPane(draw);
        frame.addKeyListener(std);    // JLabel cannot get keyboard focus
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);            // closes all windows
        // frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);      // closes only current window
        frame.setTitle("Standard Draw");
        frame.setJMenuBar(createMenuBar());
        frame.pack();
        frame.requestFocusInWindow();
        frame.setVisible(true);
    }

    // создание меню
    private static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        JMenuItem menuItem1 = new JMenuItem(" Save...   ");
        menuItem1.addActionListener(std);
        menuItem1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(menuItem1);
        return menuBar;
    }


   /*************************************************************************
    *  User and screen coordinate systems
    *************************************************************************/

    /**
     * Устанавливает координатнуую шкалу по x на значение по умолчанию (между 0.0 и 1.0).
     */
    public static void setXscale() { setXscale(DEFAULT_XMIN, DEFAULT_XMAX); }

    /**
     * Устанавливает координатнуую шкалу по y на значение по умолчанию (между 0.0 и 1.0).
     */
    public static void setYscale() { setYscale(DEFAULT_YMIN, DEFAULT_YMAX); }

    /**
     * Устанавливает координатнуую шкалу по x на значение (10% границы добавляются)
     * @param min минимальное значение шкалы x
     * @param max максимальное значение шкалы x
     */
    public static void setXscale(double min, double max) {
        double size = max - min;
        synchronized (mouseLock) {
            xmin = min - BORDER * size;
            xmax = max + BORDER * size;
        }
    }

    /**
     * Устанавливает координатнуую шкалу по y на значение (10% границы добавляются)
     * @param min минимальное значение шкалы y
     * @param max максимальное значение шкалы y
     */
    public static void setYscale(double min, double max) {
        double size = max - min;
        synchronized (mouseLock) {
            ymin = min - BORDER * size;
            ymax = max + BORDER * size;
        }
    }

    /**
     * Устанавливает координатнуую шкалу по x и y на значение (10% границы добавляются)
     * @param min минимальное значение шкал x и y
     * @param max максимальное значение шкал x и y
     */
    public static void setScale(double min, double max) {
        double size = max - min;
        synchronized (mouseLock) {
            xmin = min - BORDER * size;
            xmax = max + BORDER * size;
            ymin = min - BORDER * size;
            ymax = max + BORDER * size;
        }
    }

    // функции, которые переводят из координат пользователя в координаты экрана и наоборот
    private static double  scaleX(double x) { return width  * (x - xmin) / (xmax - xmin); }
    private static double  scaleY(double y) { return height * (ymax - y) / (ymax - ymin); }
    private static double factorX(double w) { return w * width  / Math.abs(xmax - xmin);  }
    private static double factorY(double h) { return h * height / Math.abs(ymax - ymin);  }
    private static double   userX(double x) { return xmin + x * (xmax - xmin) / width;    }
    private static double   userY(double y) { return ymax - y * (ymax - ymin) / height;   }


    /**
     * Очистка экрана в цвет по умолчанию (белый).
     */
    public static void clear() { clear(DEFAULT_CLEAR_COLOR); }
    /**
     * Очистка экрана в заданный цвет.
     * @param color цвет, в который будет закрашен фон
     */
    public static void clear(Color color) {
        offscreen.setColor(color);
        offscreen.fillRect(0, 0, width, height);
        offscreen.setColor(penColor);
        draw();
    }

    /**
     * Вернуть текущий размер радиуса карандаша
     */
    public static double getPenRadius() { return penRadius; }

    /**
     * Установить текущий размер радиуса карандаша (по умолчанию .002).
     */
    public static void setPenRadius() { setPenRadius(DEFAULT_PEN_RADIUS); }
    /**
     * Установить текущему размеру радиуса карандаша значение
     * @param r радуис карандаша
     * @throws RuntimeException если значение отрицательное
     */
    public static void setPenRadius(double r) {
        if (r < 0) throw new RuntimeException("pen radius must be positive");
        penRadius = r;
        float scaledPenRadius = (float) (r * DEFAULT_SIZE);
        BasicStroke stroke = new BasicStroke(scaledPenRadius, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        // BasicStroke stroke = new BasicStroke(scaledPenRadius);
        offscreen.setStroke(stroke);
    }

    /**
     * Получить текущее цвет карандаша
     */
    public static Color getPenColor() { return penColor; }

    /**
     * Установить текущиему цвету карандаша значение по умолчанию (черный).
     */
    public static void setPenColor() { setPenColor(DEFAULT_PEN_COLOR); }
    /**
     * Установить текущиему цвету карандаша заданное значение . Доступный следующие цвета
     * BLACK, BLUE, CYAN, DARK_GRAY, GRAY, GREEN, LIGHT_GRAY, MAGENTA,
     * ORANGE, PINK, RED, WHITE, and YELLOW.
     * @param color цвет карандаша
     */
    public static void setPenColor(Color color) {
        penColor = color;
        offscreen.setColor(penColor);
    }

    /**
     * Получить текущий шрифт
     */
    public static Font getFont() { return font; }

    /**
     * Установить текущий шрифт шрифтом по умолчанию (sans serif, 16 пунктов).
     */
    public static void setFont() { setFont(DEFAULT_FONT); }

    /**
     * Устанавливает заданный шрифт
     * @param f шрифт
     */
    public static void setFont(Font f) { font = f; }


   /*************************************************************************
    *  Рисование геометрических объектов
    *************************************************************************/

    /**
     * Рисует линию из (x0, y0) в (x1, y1)
     * @param x0 координата-x начальное точки
     * @param y0 координата-y начальной точки
     * @param x1 координата-x конечной точки
     * @param y1 координата-y конечной точки
     */
    public static void line(double x0, double y0, double x1, double y1) {
        offscreen.draw(new Line2D.Double(scaleX(x0), scaleY(y0), scaleX(x1), scaleY(y1)));
        draw();
    }

    /**
     * Рисует пиксел (x, y).
     * @param x координата-x пиксела
     * @param y координата-y пиксела
     */
    private static void pixel(double x, double y) {
        offscreen.fillRect((int) Math.round(scaleX(x)), (int) Math.round(scaleY(y)), 1, 1);
    }

    /**
     * Рисует точку (x, y).
     * @param x координата-x точки
     * @param y координата-y точки
     */
    public static void point(double x, double y) {
        double xs = scaleX(x);
        double ys = scaleY(y);
        double r = penRadius;
        float scaledPenRadius = (float) (r * DEFAULT_SIZE);

        // double ws = factorX(2*r);
        // double hs = factorY(2*r);
        // if (ws <= 1 && hs <= 1) pixel(x, y);
        if (scaledPenRadius <= 1) pixel(x, y);
        else offscreen.fill(new Ellipse2D.Double(xs - scaledPenRadius/2, ys - scaledPenRadius/2,
                                                 scaledPenRadius, scaledPenRadius));
        draw();
    }

    /**
     * Рисует окружность с радиусом r и центром (x, y).
     * @param x координата-x центра окружности
     * @param y координата-y центра окружности
     * @param r радиус окружности
     * @throws RuntimeException если радиус окружности отрицательный
     */
    public static void circle(double x, double y, double r) {
        if (r < 0) throw new RuntimeException("circle radius can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Ellipse2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }

    /**
     *  Рисует и закрашивает окружность с радиусом r и центром (x, y).
     * @param x координата-x центра окружности
     * @param y координата-y центра окружности
     * @param r радиус окружности
     * @throws RuntimeException если радиус окружности отрицательный
     */
    public static void filledCircle(double x, double y, double r) {
        if (r < 0) throw new RuntimeException("circle radius can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Ellipse2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }


    /**
     * Рисует эллипс с заданными полуосями и центром (x, y).
     * @param x координата-x центра эллипса
     * @param y координата-y центра эллипса
     * @param semiMajorAxis большая полуось эллипса
     * @param semiMinorAxis малая полуось эллипса
     * @throws RuntimeException если одна из осей отрицательная
     */
    public static void ellipse(double x, double y, double semiMajorAxis, double semiMinorAxis) {
        if (semiMajorAxis < 0) throw new RuntimeException("ellipse semimajor axis can't be negative");
        if (semiMinorAxis < 0) throw new RuntimeException("ellipse semiminor axis can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*semiMajorAxis);
        double hs = factorY(2*semiMinorAxis);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Ellipse2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }

    /**
     * Рисует и закрашивает эллипс с заданными полуосями и центром (x, y).
     * @param x координата-x центра эллипса
     * @param y координата-y центра эллипса
     * @param semiMajorAxis большая полуось эллипса
     * @param semiMinorAxis малая полуось эллипса
     * @throws RuntimeException если одна из осей отрицательная
     */
    public static void filledEllipse(double x, double y, double semiMajorAxis, double semiMinorAxis) {
        if (semiMajorAxis < 0) throw new RuntimeException("ellipse semimajor axis can't be negative");
        if (semiMinorAxis < 0) throw new RuntimeException("ellipse semiminor axis can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*semiMajorAxis);
        double hs = factorY(2*semiMinorAxis);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Ellipse2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }


    /**
     * Рисует дугу окружности радиуса r с центром в (x, y), начиная с угла angle1 до angle2 (в градусах).
     * @param x координата-x центра окружности
     * @param y координата-y центра окружности
     * @param r радиус окружности
     * @param angle1 начальный угол
     * @param angle2 конечный угол 
     * @throws RuntimeException если радиус отрицательный
     */
    public static void arc(double x, double y, double r, double angle1, double angle2) {
        if (r < 0) throw new RuntimeException("arc radius can't be negative");
        while (angle2 < angle1) angle2 += 360;
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Arc2D.Double(xs - ws/2, ys - hs/2, ws, hs, angle1, angle2 - angle1, Arc2D.OPEN));
        draw();
    }

    /**
     * Рисует квадрат с длиной стороны 2r и центром (x, y).
     * @param x координата-x центра квадрата
     * @param y координата-y центра квадрата
     * @param r половина длины стороны квадрата
     * @throws RuntimeException если r отрицательно
     */
    public static void square(double x, double y, double r) {
        if (r < 0) throw new RuntimeException("square side length can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Rectangle2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }

    /**
     * Рисует и закрашивает квадрат с длиной стороны 2r и центром (x, y).
     * @param x координата-x центра квадрата
     * @param y координата-y центра квадрата
     * @param r половина длины стороны квадрата
     * @throws RuntimeException если r отрицательно
     */
    public static void filledSquare(double x, double y, double r) {
        if (r < 0) throw new RuntimeException("square side length can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Rectangle2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }


    /**
     * Рисует прямоугольник с заданными высотой и шириной с центром в точке (x, y).
     * @param x координата-x центра прямоугольника
     * @param y координата-y центра прямоугольника
     * @param halfWidth половина ширины прямоугольника
     * @param halfHeight половина высоты прямоугольника
     * @throws RuntimeException если половина высоты или половина ширины отрицательные
     */
    public static void rectangle(double x, double y, double halfWidth, double halfHeight) {
        if (halfWidth  < 0) throw new RuntimeException("half width can't be negative");
        if (halfHeight < 0) throw new RuntimeException("half height can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*halfWidth);
        double hs = factorY(2*halfHeight);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Rectangle2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }

    /**
     * Рисует и закрашивает прямоугольник с заданными высотой и шириной с центром в точке (x, y).
     * @param x координата-x центра прямоугольника
     * @param y координата-y центра прямоугольника
     * @param halfWidth половина ширины прямоугольника
     * @param halfHeight половина высоты прямоугольника
     * @throws RuntimeException если половина высоты или половина ширины отрицательные
     */
    public static void filledRectangle(double x, double y, double halfWidth, double halfHeight) {
        if (halfWidth  < 0) throw new RuntimeException("half width can't be negative");
        if (halfHeight < 0) throw new RuntimeException("half height can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*halfWidth);
        double hs = factorY(2*halfHeight);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Rectangle2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }


    /**
     * Рисует многоугольник по заданным координатам (x[i], y[i]).
     * @param x массив координат-x многоугольника
     * @param y массив координат-y многоугольника
     */
    public static void polygon(double[] x, double[] y) {
        int N = x.length;
        GeneralPath path = new GeneralPath();
        path.moveTo((float) scaleX(x[0]), (float) scaleY(y[0]));
        for (int i = 0; i < N; i++)
            path.lineTo((float) scaleX(x[i]), (float) scaleY(y[i]));
        path.closePath();
        offscreen.draw(path);
        draw();
    }

    /**
     * Рисует и закрашивает многоугольник по заданным координатам (x[i], y[i]).
     * @param x массив координат-x многоугольника
     * @param y массив координат-y многоугольника
     */
    public static void filledPolygon(double[] x, double[] y) {
        int N = x.length;
        GeneralPath path = new GeneralPath();
        path.moveTo((float) scaleX(x[0]), (float) scaleY(y[0]));
        for (int i = 0; i < N; i++)
            path.lineTo((float) scaleX(x[i]), (float) scaleY(y[i]));
        path.closePath();
        offscreen.fill(path);
        draw();
    }



   /*************************************************************************
    *  Drawing images.
    *************************************************************************/

    // get an image from the given filename
    private static Image getImage(String filename) {

        // to read from file
        ImageIcon icon = new ImageIcon(filename);

        // try to read from URL
        if ((icon == null) || (icon.getImageLoadStatus() != MediaTracker.COMPLETE)) {
            try {
                URL url = new URL(filename);
                icon = new ImageIcon(url);
            } catch (Exception e) { /* not a url */ }
        }

        // in case file is inside a .jar
        if ((icon == null) || (icon.getImageLoadStatus() != MediaTracker.COMPLETE)) {
            URL url = StdDraw.class.getResource(filename);
            if (url == null) throw new RuntimeException("image " + filename + " not found");
            icon = new ImageIcon(url);
        }

        return icon.getImage();
    }

    /**
     * Рисует изображение (gif, jpg, or png) с центром в (x, y).
     * @param x координата-x изоюражения
     * @param y координата-у изоюражения
     * @param s имя файла с изображением, например, "ball.gif"
     * @throws RuntimeException если изображение некорректное
     */
    public static void picture(double x, double y, String s) {
        Image image = getImage(s);
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = image.getWidth(null);
        int hs = image.getHeight(null);
        if (ws < 0 || hs < 0) throw new RuntimeException("image " + s + " is corrupt");

        offscreen.drawImage(image, (int) Math.round(xs - ws/2.0), (int) Math.round(ys - hs/2.0), null);
        draw();
    }
    /**
     * Рисует изображение (gif, jpg, or png) с центром в (x, y) повернутое на 
     * заданное число градусов.
     * @param x координата-x изоюражения
     * @param y координата-у изоюражения
     * @param s имя файла с изображением, например, "ball.gif"
     * @param degrees число градусов, на которое нужно повернуть изображение против часовой стрелки
     * @throws RuntimeException если изображение некорректное
     */
    public static void picture(double x, double y, String s, double degrees) {
        Image image = getImage(s);
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = image.getWidth(null);
        int hs = image.getHeight(null);
        if (ws < 0 || hs < 0) throw new RuntimeException("image " + s + " is corrupt");

        offscreen.rotate(Math.toRadians(-degrees), xs, ys);
        offscreen.drawImage(image, (int) Math.round(xs - ws/2.0), (int) Math.round(ys - hs/2.0), null);
        offscreen.rotate(Math.toRadians(+degrees), xs, ys);

        draw();
    }

    /**
     * Рисует изобарежение (gif, jpg, or png) с центром в (x, y), с изменением масштаба на w на h.
     * @param x координата-x изоюражения
     * @param y координата-у изоюражения
     * @param s имя файла с изображением, например, "ball.gif"
     * @param w ширина изображения
     * @param h высота изображения
     * @throws RuntimeException если изображение некорректное или ширина/высота отрицательные
     */
    public static void picture(double x, double y, String s, double w, double h) {
        Image image = getImage(s);
        double xs = scaleX(x);
        double ys = scaleY(y);
        if (w < 0) throw new RuntimeException("width is negative: " + w);
        if (h < 0) throw new RuntimeException("height is negative: " + h);
        double ws = factorX(w);
        double hs = factorY(h);
        if (ws < 0 || hs < 0) throw new RuntimeException("image " + s + " is corrupt");
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else {
            offscreen.drawImage(image, (int) Math.round(xs - ws/2.0),
                                       (int) Math.round(ys - hs/2.0),
                                       (int) Math.round(ws),
                                       (int) Math.round(hs), null);
        }
        draw();
    }

    
    /**
     * Рисует изобарежение (gif, jpg, or png) с центром в (x, y) повернутое на 
     * заданное число градусов, с изменением масштаба на w на h.
     * @param x координата-x изоюражения
     * @param y координата-у изоюражения
     * @param s имя файла с изображением, например, "ball.gif"
     * @param w ширина изображения
     * @param h высота изображения
     * @param degrees число градусов, на которое нужно повернуть изображение против часовой стрелки
     * @throws RuntimeException если изображение некорректное или ширина/высота отрицательные
     */
    public static void picture(double x, double y, String s, double w, double h, double degrees) {
        Image image = getImage(s);
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(w);
        double hs = factorY(h);
        if (ws < 0 || hs < 0) throw new RuntimeException("image " + s + " is corrupt");
        if (ws <= 1 && hs <= 1) pixel(x, y);

        offscreen.rotate(Math.toRadians(-degrees), xs, ys);
        offscreen.drawImage(image, (int) Math.round(xs - ws/2.0),
                                   (int) Math.round(ys - hs/2.0),
                                   (int) Math.round(ws),
                                   (int) Math.round(hs), null);
        offscreen.rotate(Math.toRadians(+degrees), xs, ys);

        draw();
    }


   /*************************************************************************
    *  Рисование текста
    *************************************************************************/

    /**
     * Пишет данный текст текущим шрифтом, центрирует по (x, y).
     * @param x координата-x центра текста
     * @param y координата-y центра текста
     * @param s текст
     */
    public static void text(double x, double y, String s) {
        offscreen.setFont(font);
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = metrics.stringWidth(s);
        int hs = metrics.getDescent();
        offscreen.drawString(s, (float) (xs - ws/2.0), (float) (ys + hs));
        draw();
    }

    /**
     * Пишет данный текст текущим шрифтом, центрирует по (x, y) и поворачивает на 
     * заданное число градусов.
     * @param x координата-x центра текста
     * @param y координата-y центра текста
     * @param s текст
     * @param degrees число градусов, на которое нужно повернуть изображение против часовой стрелки
     */

    public static void text(double x, double y, String s, double degrees) {
        double xs = scaleX(x);
        double ys = scaleY(y);
        offscreen.rotate(Math.toRadians(-degrees), xs, ys);
        text(x, y, s);
        offscreen.rotate(Math.toRadians(+degrees), xs, ys);
    }


    /**
     * Пишет данный текст текущим шрифтом, выравнивает по левому краю (x, y).
     * @param x координата-x центра текста
     * @param y координата-y центра текста
     * @param s текст
     */
    public static void textLeft(double x, double y, String s) {
        offscreen.setFont(font);
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int hs = metrics.getDescent();
        offscreen.drawString(s, (float) (xs), (float) (ys + hs));
        draw();
    }

        /**
     * Пишет данный текст текущим шрифтом, выравнивает по правому краю (x, y).
     * @param x координата-x центра текста
     * @param y координата-y центра текста
     * @param s текст
     */
    public static void textRight(double x, double y, String s) {
        offscreen.setFont(font);
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = metrics.stringWidth(s);
        int hs = metrics.getDescent();
        offscreen.drawString(s, (float) (xs - ws), (float) (ys + hs));
        draw();
    }



    /**
     * Отображает на экране, задает паузу в t миллисеунд и продолжает 
     * <em>режим анимации</em>: дальнейшие вызовы методов рисования,
     * такие как <tt>line()</tt>, <tt>circle()</tt> и <tt>square()</tt>
     * не будут отображаться на экране до тех пор, пока не будет вызван следующий <tt>show()</tt>.
     * Это полезно для создания анимации (очистить экран, нарисовать группу фигур,
     * отобразить на экране на фиксированный отрезок времени и повторить). 
     * Также метод ускоряет отрисовку большого количества фигур (вызовите <tt>show(0)</tt> 
     * для сравнения отрисовки на экране каждой фигуры и отрисовки за один раз).
     * @param t число миллисекунд
     */
    public static void show(int t) {
        defer = false;
        draw();
        try { Thread.currentThread().sleep(t); }
        catch (InterruptedException e) { System.out.println("Error sleeping"); }
        defer = true;
    }

    /**
     * Отрисовывает на экране и выключает режим анимации:
     * последующие вызовы для отрисовки, такие как <tt>line()</tt>, 
     * <tt>circle()</tt>, and <tt>square()</tt>
     * будут отображаться на экране сразу после вызова. Это режим по умолчанию.
     */
    public static void show() {
        defer = false;
        draw();
    }

    // рисует на экране, если defer = false
    private static void draw() {
        if (defer) return;
        onscreen.drawImage(offscreenImage, 0, 0, null);
        frame.repaint();
    }


   /*************************************************************************
    *  Сохранение изображения в файл.
    *************************************************************************/

    /**
     * Сохраняет экранное изображение в файл - расширение должно быть png, jpg или gif.
     * @param filename имя файла с правильный расширением
     */
    public static void save(String filename) {
        File file = new File(filename);
        String suffix = filename.substring(filename.lastIndexOf('.') + 1);

        // png files
        if (suffix.toLowerCase().equals("png")) {
            try { ImageIO.write(onscreenImage, suffix, file); }
            catch (IOException e) { e.printStackTrace(); }
        }

        // need to change from ARGB to RGB for jpeg
        // reference: http://archives.java.sun.com/cgi-bin/wa?A2=ind0404&L=java2d-interest&D=0&P=2727
        else if (suffix.toLowerCase().equals("jpg")) {
            WritableRaster raster = onscreenImage.getRaster();
            WritableRaster newRaster;
            newRaster = raster.createWritableChild(0, 0, width, height, 0, 0, new int[] {0, 1, 2});
            DirectColorModel cm = (DirectColorModel) onscreenImage.getColorModel();
            DirectColorModel newCM = new DirectColorModel(cm.getPixelSize(),
                                                          cm.getRedMask(),
                                                          cm.getGreenMask(),
                                                          cm.getBlueMask());
            BufferedImage rgbBuffer = new BufferedImage(newCM, newRaster, false,  null);
            try { ImageIO.write(rgbBuffer, suffix, file); }
            catch (IOException e) { e.printStackTrace(); }
        }

        else {
            System.out.println("Invalid image file type: " + suffix);
        }
    }


    /**
     * Этот метод не может быть вызван напрямую
     */
    public void actionPerformed(ActionEvent e) {
        FileDialog chooser = new FileDialog(StdDraw.frame, "Use a .png or .jpg extension", FileDialog.SAVE);
        chooser.setVisible(true);
        String filename = chooser.getFile();
        if (filename != null) {
            StdDraw.save(chooser.getDirectory() + File.separator + chooser.getFile());
        }
    }


   /*************************************************************************
    *  Взаимодействие с мышью.
    *************************************************************************/

    /**
     * Была ли нажата кнопка мыши?
     * @return true или false
     */
    public static boolean mousePressed() {
        synchronized (mouseLock) {
            return mousePressed;
        }
    }

    /**
     * Координата по x курсора мыши
     * @return координаты по x курсора мыши
     */
    public static double mouseX() {
        synchronized (mouseLock) {
            return mouseX;
        }
    }

    /**
     * Координата по y курсора мыши
     * @return координаты по y курсора мыши
     */
    public static double mouseY() {
        synchronized (mouseLock) {
            return mouseY;
        }
    }


    /**
     * Этот метод не может быть вызван напрямую
     */
    public void mouseClicked(MouseEvent e) { }

    /**
    * Этот метод не может быть вызван напрямую
     */
    public void mouseEntered(MouseEvent e) { }

    /**
     * Этот метод не может быть вызван напрямую
     */
    public void mouseExited(MouseEvent e) { }

    /**
     * Этот метод не может быть вызван напрямую
     */
    public void mousePressed(MouseEvent e) {
        synchronized (mouseLock) {
            mouseX = StdDraw.userX(e.getX());
            mouseY = StdDraw.userY(e.getY());
            mousePressed = true;
        }
    }

    /**
     * Этот метод не может быть вызван напрямую
     */
    public void mouseReleased(MouseEvent e) {
        synchronized (mouseLock) {
            mousePressed = false;
        }
    }

    /**
     * Этот метод не может быть вызван напрямую
     */
    public void mouseDragged(MouseEvent e)  {
        synchronized (mouseLock) {
            mouseX = StdDraw.userX(e.getX());
            mouseY = StdDraw.userY(e.getY());
        }
    }

    /**
     * Этот метод не может быть вызван напрямую
     */
    public void mouseMoved(MouseEvent e) {
        synchronized (mouseLock) {
            mouseX = StdDraw.userX(e.getX());
            mouseY = StdDraw.userY(e.getY());
        }
    }


   /*************************************************************************
    *  Взаимодействие с клавиатурой.
    *************************************************************************/

    /**
     * Нажал ли пользователь кнопку
     * @return true если пользователь нажал кнопку, false в противном случае
     */
    public static boolean hasNextKeyTyped() {
        synchronized (keyLock) {
            return !keysTyped.isEmpty();
        }
    }

    /**
     * Метод возвращает символ Unicode, соответствующий напечатанному 
     * символу corresponding (например 'a' или 'A').
     * @return Unicode-символ нажатой клавиши
     */
    public static char nextKeyTyped() {
        synchronized (keyLock) {
            return keysTyped.removeLast();
        }
    }

    /**
     * Метод принимает код в качестве аргумента. Он может обрабатывать функциональный клавиши
     * (такие как F1 и кнопки управления курсором) и клавиши-модификаторы (такие как shift и control).
     * Более подробно смотрите  <a href = "http://download.oracle.com/javase/6/docs/api/java/awt/event/KeyEvent.html">KeyEvent.java</a>
     * описание кодов.
     * @return true если текущий код был нажат, false в противном случае
     */
    public static boolean isKeyPressed(int keycode) {
        synchronized (keyLock) {
            return keysDown.contains(keycode);
        }
    }


    /**
     * Этот метод не может быть вызван напрямую
     */
    public void keyTyped(KeyEvent e) {
        synchronized (keyLock) {
            keysTyped.addFirst(e.getKeyChar());
        }
    }

    /**
     * Этот метод не может быть вызван напрямую
     */
    public void keyPressed(KeyEvent e) {
        synchronized (keyLock) {
            keysDown.add(e.getKeyCode());
        }
    }

    /**
     * Этот метод не может быть вызван напрямую
     */
    public void keyReleased(KeyEvent e) {
        synchronized (keyLock) {
            keysDown.remove(e.getKeyCode());
        }
    }




    /**
     * Проверка
     */
    public static void main(String[] args) {
        StdDraw.square(.2, .8, .1);
        StdDraw.filledSquare(.8, .8, .2);
        StdDraw.circle(.8, .2, .2);

        StdDraw.setPenColor(StdDraw.BOOK_RED);
        StdDraw.setPenRadius(.02);
        StdDraw.arc(.8, .2, .1, 200, 45);

        // синий ромб
        StdDraw.setPenRadius();
        StdDraw.setPenColor(StdDraw.BOOK_BLUE);
        double[] x = { .1, .2, .3, .2 };
        double[] y = { .2, .3, .2, .1 };
        StdDraw.filledPolygon(x, y);

        // текст
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.text(0.2, 0.5, "черный текст");
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(0.8, 0.8, "белый текст");
    }

}


