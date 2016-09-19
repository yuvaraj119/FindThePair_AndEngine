package com.yuvaraj.findthepair_andengine;

import android.util.DisplayMetrics;
import android.util.Log;


import com.yuvaraj.findthepair_andengine.pojos.PairPojo;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.DelayModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.debug.Debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FindThePair extends SimpleBaseGameActivity {
    // ===========================================================
    // Constants
    // ===========================================================

    private static int CAMERA_WIDTH;
    private static int CAMERA_HEIGHT;

    // ===========================================================
    // Fields
    // ===========================================================

    private BuildableBitmapTextureAtlas mBuildableBitmapTextureAtlas;

    private int numCols = 4;
    private int numRows = 3;
    private int numPairsOnBoard = (numRows * numCols) / 2;

    private HashMap<String, TiledTextureRegion> cardTileTextures = new HashMap<String, TiledTextureRegion>();
    private HashMap<TiledSprite, String> spriteToName = new HashMap<TiledSprite, String>();
    private List<PairPojo> listTile = new ArrayList<PairPojo>();
    private List<TiledSprite> flippedTileSpriteList = new ArrayList<TiledSprite>();
    private int numCardGFXPairs = 0;

    private Scene scene;
    private Font mFont;

    private Text scoreNumberText;

    private Camera camera;

    private int score = 0;
    private boolean touchEnabled = true;

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public EngineOptions onCreateEngineOptions() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        CAMERA_WIDTH = metrics.widthPixels;
        CAMERA_HEIGHT = metrics.heightPixels;

        camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

        final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.camera);

        return engineOptions;

    }

    @Override
    public void onCreateResources() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        this.mBuildableBitmapTextureAtlas = new BuildableBitmapTextureAtlas(this.getTextureManager(), 2048, 2048, TextureOptions.BILINEAR);

        numCardGFXPairs = 4;

        for (int i = 0; i < numCardGFXPairs; i++) {
            cardTileTextures.put(i + "-a", BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBuildableBitmapTextureAtlas, this, i + "-a.png", 2, 1));
            cardTileTextures.put(i + "-b", BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBuildableBitmapTextureAtlas, this, i + "-b.png", 2, 1));
            PairPojo pairPojo = new PairPojo();
            pairPojo.setTileValue(i + "-a");
            pairPojo.setEnable(true);
            pairPojo.setFlipped(false);
            this.listTile.add(pairPojo);
            pairPojo = null;
            PairPojo pairPojoB = new PairPojo();
            pairPojoB.setTileValue(i + "-b");
            pairPojoB.setEnable(true);
            pairPojoB.setFlipped(false);
            this.listTile.add(pairPojoB);
            pairPojoB = null;
        }
        try {
            this.mBuildableBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 1));
            this.mBuildableBitmapTextureAtlas.load();
        } catch (ITextureAtlasBuilder.TextureAtlasBuilderException e) {
            Debug.e(e);
        }

        FontFactory.setAssetBasePath("font/");

        final ITexture fontTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
        this.mFont = FontFactory.createFromAsset(this.getFontManager(), fontTexture, this.getAssets(), "Plok.ttf", 32, true, android.graphics.Color.WHITE);
        this.mFont.load();

    }

    @Override
    public Scene onCreateScene() {
        this.mEngine.registerUpdateHandler(new FPSLogger());

        this.scene = new Scene();

        final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
        final Text centerText = new Text(20, 10, this.mFont, "find the pairs.", new TextOptions(HorizontalAlign.CENTER), vertexBufferObjectManager);
        this.scene.attachChild(centerText);

        this.scene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));

        final Text scoreText = new Text(71, 1020, this.mFont, "Score is:", vertexBufferObjectManager);
        this.scene.attachChild(scoreText);

        float scoreTextWidth = scoreText.getWidth();

        this.scoreNumberText = new Text(scoreTextWidth + 100, 1020, this.mFont, "1234567890", vertexBufferObjectManager);
        this.scoreNumberText.setText("0");
        this.scene.attachChild(scoreNumberText);

        Collections.shuffle(listTile);

        //width=233
        //height=372
        int a, b = 50, countAb = 0;
        for (int i = 0; i < 2; i++) {
            a = 233;
            for (int j = 0; j < 4; j++) {
                addTileCard(listTile.get(countAb).getTileValue(), countAb, a, b);
                a = a + 233;
                countAb++;
            }
            b = b + 372;
        }

        return this.scene;
    }

    private void addTileCard(final String tileValue, final int count, final int pX, final int pY) {
        final TiledSprite tiledSprite = new TiledSprite(pX, pY, 233, 372, cardTileTextures.get(tileValue), this.getVertexBufferObjectManager()) {
            final TiledSprite s = this;

            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if (pSceneTouchEvent.isActionDown() && touchEnabled) {
                    touchEnabled = false;
                    flippedTileSpriteList.add(s);
                    scene.unregisterTouchArea(s);
                    flip(s);
                }

                return true;
            }
        };
        tiledSprite.setTag(count);
        tiledSprite.setCurrentTileIndex(1);
        tiledSprite.setScale(0.97f);
        this.scene.registerTouchArea(tiledSprite);
        this.scene.attachChild(tiledSprite);
        this.scene.setTouchAreaBindingOnActionDownEnabled(true);
        spriteToName.put(tiledSprite, tileValue);
    }

    private void flip(TiledSprite card) {
        final TiledSprite c = card;
        c.registerEntityModifier(
                new ScaleModifier(0.2f, 0.97f, 0, 0.97f, 0.92f) {
                    @Override
                    protected void onModifierFinished(IEntity pItem) {
                        c.setCurrentTileIndex(0);
                        c.registerEntityModifier(
                                new ScaleModifier(0.2f, 0, 0.97f, .92f, 0.97f) {
                                    @Override
                                    protected void onModifierFinished(IEntity pItem) {

                                        matchOrReset();

                                    }
                                }
                        );
                    }
                }
        );
    }

    private void unFlip() {
        for (TiledSprite ts : flippedTileSpriteList) {
            final TiledSprite t = ts;
            this.scene.registerTouchArea(t);
            t.registerEntityModifier(
                    new DelayModifier(1.24f) {
                        @Override
                        protected void onModifierFinished(IEntity pItem) {
                            t.registerEntityModifier(
                                    new ScaleModifier(0.2f, 0.97f, 0, 0.97f, 0.92f) {
                                        @Override
                                        protected void onModifierFinished(IEntity pItem) {
                                            t.setCurrentTileIndex(1);
                                            t.registerEntityModifier(new ScaleModifier(0.2f, 0, 0.97f, .92f, 0.97f));
                                        }
                                    }
                            );
                        }
                    }
            );
        }

        touchEnabled = true;

    }

    private void matchOrReset() {

        if (flippedTileSpriteList.size() >= 2) {
            boolean match = false;

            TiledSprite one = flippedTileSpriteList.get(0);
            TiledSprite two = flippedTileSpriteList.get(1);
            String oneIndex = spriteToName.get(one).split("-")[0];
            String oneType = spriteToName.get(one).split("-")[1];
            String twoIndex = spriteToName.get(two).split("-")[0];
            String twoType = spriteToName.get(two).split("-")[1];

            if (oneType.equals(twoType)) {
                match = true;
            }

            if (match) {
                updateScore();
                matchAnimation();
                if (score == numPairsOnBoard) {
                    Log.e("Over", "Game Over");
                } else {
                    flippedTileSpriteList.clear();
                }

            } else {
                unFlip();
                flippedTileSpriteList.clear();
            }

        } else {
            touchEnabled = true;
        }
    }

    private void updateScore() {
        this.score = this.score + 1;
        this.scoreNumberText.setText("" + score);
    }

    private void removeMatch(final TiledSprite tileSprite) {
        runOnUpdateThread(new Runnable() {
            @Override
            public void run() {
                scene.detachChild(tileSprite);
                tileSprite.dispose();
            }
        });
    }

    private void matchAnimation() {
        for (final TiledSprite t : flippedTileSpriteList) {
            t.registerEntityModifier(new SequenceEntityModifier(
                    new DelayModifier(1.26f),
                    new ScaleModifier(0.2f, 0.97f, 0, 0.97f, 0.92f)
            ) {
                @Override
                protected void onModifierFinished(IEntity pItem) {
                    removeMatch(t);
                    touchEnabled = true;
                }
            });
        }
    }

// ===========================================================
// Methods
// ===========================================================

// ===========================================================
// Inner and Anonymous Classes
// ===========================================================
}
