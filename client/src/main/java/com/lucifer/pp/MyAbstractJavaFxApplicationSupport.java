package com.lucifer.pp;

import java.awt.SystemTray;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import de.felixroske.jfxsupport.AbstractFxmlView;
import de.felixroske.jfxsupport.GUIState;
import de.felixroske.jfxsupport.PropertyReaderHelper;
import de.felixroske.jfxsupport.SplashScreen;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public abstract class MyAbstractJavaFxApplicationSupport extends Application {
    private static Logger LOGGER = LoggerFactory.getLogger(MyAbstractJavaFxApplicationSupport.class);
    private static String[] savedArgs = new String[0];
    static Class<? extends AbstractFxmlView> savedInitialView;
    static SplashScreen splashScreen;
    private static ConfigurableApplicationContext applicationContext;
    private static List<Image> icons = new ArrayList();
    private final List<Image> defaultIcons = new ArrayList();
    private final CompletableFuture<Runnable> splashIsShowing = new CompletableFuture();

    protected MyAbstractJavaFxApplicationSupport() {
    }

    public static Stage getStage() {
        return GUIState.getStage();
    }

    public static Scene getScene() {
        return GUIState.getScene();
    }

    public static HostServices getAppHostServices() {
        return GUIState.getHostServices();
    }

    public static SystemTray getSystemTray() {
        return GUIState.getSystemTray();
    }

    public static void showView(Class<? extends AbstractFxmlView> window, Modality mode) {
        AbstractFxmlView view = applicationContext.getBean(window);
        Stage newStage = new Stage();
        Scene newScene;
        if (view.getView().getScene() != null) {
            newScene = view.getView().getScene();
        } else {
            newScene = new Scene(view.getView());
        }

        newStage.setScene(newScene);
        newStage.initModality(mode);
        newStage.initOwner(getStage());
//        try {
//            Class<?> parent =  view.getClass().getSuperclass();
//            Method getDefaultTitle = parent.getDeclaredMethod("getDefaultTitle");
//            getDefaultTitle.setAccessible(true);
//            Method getDefaultStyle = parent.getDeclaredMethod("getDefaultStyle");
//            getDefaultStyle.setAccessible(true);
//            newStage.setTitle((String) getDefaultTitle.invoke(view));
//            newStage.initStyle((StageStyle) getDefaultStyle.invoke(view));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        newStage.showAndWait();
    }

    public static void showView(Class<? extends AbstractFxmlView> window, Modality mode, Callback<WindowEvent,Void> closeCallback) {
        AbstractFxmlView view = applicationContext.getBean(window);
        Stage newStage = new Stage();
        Scene newScene;
        if (view.getView().getScene() != null) {
            newScene = view.getView().getScene();
        } else {
            newScene = new Scene(view.getView());
        }

        newStage.setOnCloseRequest(windowEvent -> {
            closeCallback.call(windowEvent);
            newStage.close();
        });

        newStage.setScene(newScene);
        newStage.initModality(mode);
        newStage.initOwner(getStage());
        newStage.showAndWait();
    }

    private void loadIcons(ConfigurableApplicationContext ctx) {
        try {
            List<String> fsImages = PropertyReaderHelper.get(ctx.getEnvironment(), "javafx.appicons");
            if (!fsImages.isEmpty()) {
                fsImages.forEach((s) -> {
                    Image img = new Image(this.getClass().getResource(s).toExternalForm());
                    icons.add(img);
                });
            } else {
                icons.addAll(this.defaultIcons);
            }
        } catch (Exception var3) {
            LOGGER.error("Failed to load icons: ", var3);
        }

    }

    public void init() throws Exception {
        this.defaultIcons.addAll(this.loadDefaultIcons());
        CompletableFuture.supplyAsync(() -> {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            return SpringApplication.run(this.getClass(), savedArgs);
        }).whenComplete((ctx, throwable) -> {
            if (throwable != null) {
                LOGGER.error("Failed to load spring application context: ", throwable);
                Platform.runLater(() -> {
                    showErrorAlert(throwable);
                });
            } else {
                Platform.runLater(() -> {
                    this.loadIcons(ctx);
                    this.launchApplicationView(ctx);
                });
            }

        }).thenAcceptBothAsync(this.splashIsShowing, (ctx, closeSplash) -> {
            Platform.runLater(closeSplash);
        });
    }

    public void start(Stage stage) throws Exception {
        GUIState.setStage(stage);
        Method setHostServices = GUIState.class.getDeclaredMethod("setHostServices",HostServices.class);
        setHostServices.setAccessible(true);
        setHostServices.invoke(null,this.getHostServices());
//        GUIState.setHostServices(this.getHostServices());
        Stage splashStage = new Stage(StageStyle.TRANSPARENT);
        if (splashScreen.visible()) {
            Scene splashScene = new Scene(splashScreen.getParent(), Color.TRANSPARENT);
            splashStage.setScene(splashScene);
            splashStage.getIcons().addAll(this.defaultIcons);
            splashStage.initStyle(StageStyle.TRANSPARENT);
            this.beforeShowingSplash(splashStage);
            splashStage.show();
        }

        this.splashIsShowing.complete(() -> {
            this.showInitialView();
            if (splashScreen.visible()) {
                splashStage.hide();
                splashStage.setScene((Scene)null);
            }

        });
    }

    private void showInitialView() {
        String stageStyle = applicationContext.getEnvironment().getProperty("javafx.stage.style");
        if (stageStyle != null) {
            GUIState.getStage().initStyle(StageStyle.valueOf(stageStyle.toUpperCase()));
        } else {
            GUIState.getStage().initStyle(StageStyle.DECORATED);
        }

        this.beforeInitialView(GUIState.getStage(), applicationContext);
        showView(savedInitialView);
    }

    private void launchApplicationView(ConfigurableApplicationContext ctx) {
        applicationContext = ctx;
    }

    public static void showView(Class<? extends AbstractFxmlView> newView) {
        try {
            AbstractFxmlView view = (AbstractFxmlView)applicationContext.getBean(newView);
            if (GUIState.getScene() == null) {
                GUIState.setScene(new Scene(view.getView()));
            } else {
                GUIState.getScene().setRoot(view.getView());
            }

            GUIState.getStage().setScene(GUIState.getScene());
            applyEnvPropsToView();
            GUIState.getStage().getIcons().addAll(icons);
            GUIState.getStage().show();
        } catch (Throwable var2) {
            LOGGER.error("Failed to load application: ", var2);
            showErrorAlert(var2);
        }

    }

    private static void showErrorAlert(Throwable throwable) {
        Alert alert = new Alert(AlertType.ERROR, "Oops! An unrecoverable error occurred.\nPlease contact your software vendor.\n\nThe application will stop now.\n\nError: " + throwable.getMessage(), new ButtonType[0]);
        alert.showAndWait().ifPresent((response) -> {
            Platform.exit();
        });
    }

    private static void applyEnvPropsToView() {
        ConfigurableEnvironment var10000 = applicationContext.getEnvironment();
        Stage var10003 = GUIState.getStage();
        PropertyReaderHelper.setIfPresent(var10000, "javafx.title", String.class, var10003::setTitle);
        var10000 = applicationContext.getEnvironment();
        var10003 = GUIState.getStage();
        PropertyReaderHelper.setIfPresent(var10000, "javafx.stage.width", Double.class, var10003::setWidth);
        var10000 = applicationContext.getEnvironment();
        var10003 = GUIState.getStage();
        PropertyReaderHelper.setIfPresent(var10000, "javafx.stage.height", Double.class, var10003::setHeight);
        var10000 = applicationContext.getEnvironment();
        var10003 = GUIState.getStage();
        PropertyReaderHelper.setIfPresent(var10000, "javafx.stage.resizable", Boolean.class, var10003::setResizable);
    }

    public void stop() throws Exception {
        super.stop();
        if (applicationContext != null) {
            applicationContext.close();
        }

    }

    protected static void setTitle(String title) {
        GUIState.getStage().setTitle(title);
    }

    public static void launch(Class<? extends Application> appClass, Class<? extends AbstractFxmlView> view, String[] args) {
        launch(appClass, view, new SplashScreen(), args);
    }

    /** @deprecated */
    @Deprecated
    public static void launchApp(Class<? extends Application> appClass, Class<? extends AbstractFxmlView> view, String[] args) {
        launch(appClass, view, new SplashScreen(), args);
    }

    public static void launch(Class<? extends Application> appClass, Class<? extends AbstractFxmlView> view, SplashScreen splashScreen, String[] args) {
        savedInitialView = view;
        savedArgs = args;
        if (splashScreen != null) {
            MyAbstractJavaFxApplicationSupport.splashScreen = splashScreen;
        } else {
            MyAbstractJavaFxApplicationSupport.splashScreen = new SplashScreen();
        }

        if (SystemTray.isSupported()) {
            try {
                Method setSystemTray = GUIState.class.getDeclaredMethod("setSystemTray", SystemTray.class);
                setSystemTray.setAccessible(true);
                setSystemTray.invoke(null,SystemTray.getSystemTray());
            }catch (Exception e){
                e.printStackTrace();
            }
//            GUIState.setSystemTray(SystemTray.getSystemTray());
        }

        Application.launch(appClass, args);
    }

    /** @deprecated */
    @Deprecated
    public static void launchApp(Class<? extends Application> appClass, Class<? extends AbstractFxmlView> view, SplashScreen splashScreen, String[] args) {
        launch(appClass, view, splashScreen, args);
    }

    public void beforeInitialView(Stage stage, ConfigurableApplicationContext ctx) {
    }

    public void beforeShowingSplash(Stage splashStage) {
    }

    public Collection<Image> loadDefaultIcons() {
        return Arrays.asList(new Image(this.getClass().getResource("/icons/gear_16x16.png").toExternalForm()), new Image(this.getClass().getResource("/icons/gear_24x24.png").toExternalForm()), new Image(this.getClass().getResource("/icons/gear_36x36.png").toExternalForm()), new Image(this.getClass().getResource("/icons/gear_42x42.png").toExternalForm()), new Image(this.getClass().getResource("/icons/gear_64x64.png").toExternalForm()));
    }
}
