package com.michaeldmiller.marketUI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.michaeldmiller.economicagents.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.michaeldmiller.economicagents.MarketMain.*;

public class MainInterface implements Screen {

    final MarketUI marketUI;
    Stage stage;
    Skin firstSkin;
    Market market;
    HashMap<String, Color> colorLookup;
    Label prices;
    Label errorLabel;
    TextField goodField;
    TextField costField;
    int scale;
    int frame;
    double secondFraction;
    int priceX;
    int priceY;
    int priceWidth;
    int priceHeight;
    ArrayList<GraphPoint> priceDots;
    ArrayList<Label> priceLabels;




    public MainInterface (final MarketUI marketUI) {
        this.marketUI = marketUI;
        firstSkin = new Skin(Gdx.files.internal("skin/clean-crispy-ui.json"));
        frame = 0;
        secondFraction = 0.0167;
        scale = 3;
        priceX = (int) (0.05 * marketUI.worldWidth);
        priceY = (int) (0.1 * marketUI.worldHeight);
        priceWidth = (int) (0.65 * marketUI.worldWidth);
        priceHeight = (int) (0.7 * marketUI.worldHeight);
        priceDots = new ArrayList<GraphPoint>();
        priceLabels = new ArrayList<Label>();

        // setup color lookup table
        colorLookup = new HashMap<String, Color>();
        colorLookup.put("Fish", new Color(0, 0, 0.7f, 1));
        colorLookup.put("Lumber", new Color(0, 0.7f, 0, 1));
        colorLookup.put("Grain", new Color(0.7f, 0.7f, 0, 1));
        colorLookup.put("Metal", new Color(0.7f, 0.7f, 0.7f, 1));

        stage = new Stage(new FitViewport(marketUI.worldWidth, marketUI.worldHeight));

        // add buttons
        addButtons();

        // instantiate market
        instantiateMarket();

        // make adjustment fields
        makeAdjustmentFields();

        // add price graph
        makePriceGraph();
    }



    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.9f, 0.9f, 0.9f, 1);
        Gdx.input.setInputProcessor(stage);
        frame += 1;
        // use second fraction to determine how often to call run market
        if (frame % ((int) (secondFraction * 60)) == 0) {
            try {
                runMarket(market, frame);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            graphPrices();
            removeGraphDots(priceX, priceDots);
            removeGraphLabels(priceX, priceLabels);
            prices.setText(market.getPrices().toString());

        }
        priceGraphLabels();
        stage.act(delta);
        stage.draw();
    }


    public void addButtons(){
        Button menuButton = new TextButton("Menu", firstSkin);
        menuButton.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - marketUI.standardButtonHeight);
        menuButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
        menuButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                marketUI.setScreen(marketUI.mainMenu);
                dispose();
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        stage.addActor(menuButton);

        Button printButton = new TextButton("Print", firstSkin);
        printButton.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - 2* marketUI.standardButtonHeight);
        printButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
        printButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                System.out.println(priceDots.size());
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        stage.addActor(printButton);

    }

    public void instantiateMarket(){
        MarketInfo fish = new MarketInfo("Fish", 0.35, -0.5, 0.7,
                9, 1, "Fisherman", 0.4);
        MarketInfo lumber = new MarketInfo("Lumber", 0.2, -0.5, 0.8,
                15, 1, "Lumberjack", 0.2);
        MarketInfo grain = new MarketInfo("Grain", 0.45, -0.5, 0.4,
                7, 1, "Farmer", 0.4);
        MarketInfo metal = new MarketInfo("Metal", 0.10, -1.2, 1.5,
                50, 1, "Blacksmith", 0.05);
        ArrayList<MarketInfo> currentMarketProfile = new ArrayList<MarketInfo>();
        currentMarketProfile.add(fish);
        currentMarketProfile.add(lumber);
        currentMarketProfile.add(grain);
        currentMarketProfile.add(metal);

        int numberOfAgents = 2000;
        ArrayList<Agent> marketAgents = new ArrayList<Agent>();
        marketAgents = makeAgents(currentMarketProfile, numberOfAgents);

        market = makeMarket(currentMarketProfile, marketAgents);

        prices = new Label ("Prices", firstSkin);
        prices.setPosition(100, marketUI.worldHeight - 50);
        stage.addActor(prices);

    }

    public void makeAdjustmentFields(){
        goodField = new TextField("Good", firstSkin);
        goodField.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - (int) (2.5 * marketUI.standardButtonHeight));
        stage.addActor(goodField);
        costField = new TextField("New Cost", firstSkin);
        costField.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - 3 * marketUI.standardButtonHeight);
        stage.addActor(costField);

        Button changeCostButton = new TextButton("Update Cost", firstSkin);
        changeCostButton.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - 4* marketUI.standardButtonHeight);
        changeCostButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
        changeCostButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                changePrice();
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        stage.addActor(changeCostButton);

        errorLabel = new Label ("Errors Here", firstSkin);
        errorLabel.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - (int) (4.5 * marketUI.standardButtonHeight));
        stage.addActor(errorLabel);

    }

    public void changePrice(){
        // given information in good and cost text fields, attempt to change the corresponding cost in the market
        boolean costOK = false;
        int costValue = 0;
        String good = goodField.getText();
        String cost = costField.getText();

        // make sure the user entered value is an integer
        try{
            System.out.println(cost);
            costValue = Integer.parseInt(cost);
            costOK = true;
        } catch (NumberFormatException e){
            errorLabel.setText("Not a valid cost!");
        }
        // if value is ok, check goods for match and assign cost
        if (costOK){
            for (Price p : market.getPrices()){
                if (p.getGood().equals(good)){
                    p.setOriginalCost(costValue);
                }
            }
        }

    }

    public void removeGraphDots(int xThreshold, ArrayList<GraphPoint> dots){
        // given the list of graph points
        for (int i = 0; i < dots.size(); i++){
            // if the x coordinate of the dot is at or has surpassed the threshold plus one (moving right to left)
            // remove the dot from the stage and the list of dots
            if (dots.get(i).getX() <= xThreshold + 3){
                dots.get(i).remove();
                dots.remove(i);
            }
        }
    }

    public void removeGraphLabels(int xThreshold, ArrayList<Label> labels){
        // essentially identical to removeGraphDots but for Labels, may be redundant
        for (int i = 0; i < labels.size(); i++){
            if (labels.get(i).getX() <= xThreshold + 3){
                labels.get(i).remove();
                labels.remove(i);
            }
        }
    }

    public void graphPrices(){
        // access and store all current prices, adjusted to scale with the size of the graph
        HashMap<String, Integer> priceCoordinates = new HashMap<String, Integer>();
        for (Price p : market.getPrices()){
            priceCoordinates.put(p.getGood(), (int) p.getCost() * scale);
        }
        // for each price coordinate pair, lookup the appropriate color, make a dot on the graph, then set it to scroll
        // off to the left of the screen
        for (Map.Entry<String, Integer> priceCoord : priceCoordinates.entrySet()){
            // find color
            Color dotColor = colorLookup.get(priceCoord.getKey());
            // make dot
            GraphPoint dot = new GraphPoint(priceX + priceWidth, priceCoord.getValue() + priceY, 2, 2, dotColor);

            // make actor leave screen
            MoveToAction leaveScreen = new MoveToAction();
            leaveScreen.setPosition(priceX - 10, priceY + priceCoord.getValue());
            leaveScreen.setDuration(50);
            dot.addAction(leaveScreen);
            // add actor to list of price dots
            priceDots.add(dot);

            stage.addActor(dot);

        }
    }

    public void makePriceGraph(){
        // using GraphPoints as they are general rectangles and are suited to the task
        // add x-axis
        GraphPoint xAxis = new GraphPoint(priceX, priceY, priceWidth, 3, new Color (0, 0, 0, 1));
        stage.addActor(xAxis);
        // add y-axis
        GraphPoint yAxis = new GraphPoint(priceX, priceY, 3, priceHeight, new Color (0, 0, 0, 1));
        stage.addActor(yAxis);
        // add x-ceiling
        GraphPoint xCeiling = new GraphPoint(priceX, priceY + priceHeight,
                priceWidth, 2, new Color (0, 0, 0, 1));
        stage.addActor(xCeiling);
        // add y-ceiling
        GraphPoint yCeiling = new GraphPoint(priceX + priceWidth, priceY,
                2, priceHeight, new Color (0, 0, 0, 1));
        stage.addActor(yCeiling);

    }

    public void priceGraphLabels(){
        if (frame % 200 == 0 || frame == 1){
            Label timeLabel = new Label(String.valueOf(frame), firstSkin);
            timeLabel.setPosition(priceX + priceWidth, priceY - 20);

            // add action for the labels to move to the left, following the dots
            // it is important that the duration for dot movement and label movement be the same
            MoveToAction leaveScreen = new MoveToAction();
            leaveScreen.setPosition(priceX - 10, priceY - 20);
            leaveScreen.setDuration(50);
            timeLabel.addAction(leaveScreen);
            // add labels to price label list for later removal
            priceLabels.add(timeLabel);

            stage.addActor(timeLabel);
        }

        if (frame == 1){
            // set range of prices to be covered in the graph
            int priceMax = priceHeight/scale;
            int labelNum = 1;

            for (int i = priceX; i < (priceHeight + priceX); i++) {
                // split price height into 10 sections
                if (i % priceHeight / 10 == 0){
                    // set label value to tenth of priceMax * labelNum, set position at labelNum * 1/10th of the way
                    // up the graph
                    Label quantityLabel = new Label(String.valueOf(priceMax / 10 * labelNum), firstSkin);
                    quantityLabel.setPosition(priceX + priceWidth - 20, priceY +  ((int) (priceHeight / 10) * labelNum) - 10);
                    // quantityLabel.setAlignment(Align.right);
                    // ^doesn't work

                    // add action for the labels to move to the left, following the dots. Unlike dots and tick number
                    // labels, the price labels will not be deleted upon reaching their resting place on the y-axis
                    // it is again important that the duration for dot movement and label movement be the same
                    MoveToAction leaveScreen = new MoveToAction();
                    leaveScreen.setPosition(priceX - 25, priceY + ((int) (priceHeight / 10) * labelNum) - 10);
                    leaveScreen.setDuration(50);
                    quantityLabel.addAction(leaveScreen);

                    stage.addActor(quantityLabel);

                    // add horizontal guides
                    GraphPoint xGuide = new GraphPoint(priceX, priceY + ((int) (priceHeight / 10) * labelNum),
                            priceWidth, 1, new Color (0, 0, 0, 1));
                    stage.addActor(xGuide);
                    labelNum += 1;



                }
            }
        }
    }


    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}