// $ANTLR 2.7.5 (20050128): "mathematica.g" -> "MathematicaLexer.java"$

/**
* this code is generated by ANTLR from the 'mathematica.g'-file
* @author Bernd Gonska
* @version 1.0
*/
package de.jreality.reader.mathematica;
import java.awt.*;
import java.util.*;
import de.jreality.geometry.*;
import de.jreality.math.*;
import de.jreality.scene.data.*;
import de.jreality.scene.*;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.*;
import de.jreality.util.LoggingSystem;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;

public interface MathematicaParserTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	// "Graphics3D" = 4
	int OPEN_BRACKET = 5;
	int CLOSE_BRACKET = 6;
	int OPEN_BRACE = 7;
	int CLOSE_BRACE = 8;
	int COLON = 9;
	int LITERAL_Cuboid = 10;
	int LITERAL_Text = 11;
	int STRING = 12;
	int LITERAL_Point = 13;
	int LITERAL_Line = 14;
	int LITERAL_Polygon = 15;
	int LITERAL_EdgeForm = 16;
	int LITERAL_SurfaceColor = 17;
	int LITERAL_RGBColor = 18;
	int LITERAL_Hue = 19;
	int LITERAL_GrayLevel = 20;
	int LITERAL_CMYKColor = 21;
	int LITERAL_AbsolutePointSize = 22;
	int LITERAL_AbsoluteThickness = 23;
	int LITERAL_Dashing = 24;
	int LITERAL_FaceForm = 25;
	int LITERAL_PointSize = 26;
	int LITERAL_Thickness = 27;
	int LITERAL_AbsoluteDashing = 28;
	int Option = 29;
	int LITERAL_Boxed = 30;
	int MINUS = 31;
	int LARGER = 32;
	int LITERAL_True = 33;
	int LITERAL_False = 34;
	int DDOT = 35;
	int LITERAL_Axes = 36;
	int LITERAL_Automatic = 37;
	int LITERAL_AxesLabel = 38;
	int LITERAL_Prolog = 39;
	int LITERAL_Epilog = 40;
	int LITERAL_ViewPoint = 41;
	int LITERAL_ViewCenter = 42;
	int LITERAL_FaceGrids = 43;
	int LITERAL_Ticks = 44;
	int LITERAL_TextStyle = 45;
	int LITERAL_BoxRatios = 46;
	int LITERAL_Lighting = 47;
	int LITERAL_LightSources = 48;
	int LITERAL_AmbientLight = 49;
	int LITERAL_AxesEdge = 50;
	int LITERAL_PlotRange = 51;
	int LITERAL_DefaultColor = 52;
	int LITERAL_Background = 53;
	int LITERAL_ColorOutput = 54;
	int LITERAL_AxesStyle = 55;
	int LITERAL_BoxStyle = 56;
	int LITERAL_PlotLabel = 57;
	int LITERAL_AspectRatio = 58;
	int LITERAL_DefaultFont = 59;
	int LITERAL_PlotRegion = 60;
	int LITERAL_ViewVertical = 61;
	int LITERAL_SphericalRegion = 62;
	int LITERAL_Shading = 63;
	int LITERAL_RenderAll = 64;
	int LITERAL_PolygonIntersections = 65;
	int LITERAL_DisplayFunction = 66;
	// "Plot3Matrix" = 67
	int LITERAL_ImageSize = 68;
	int LITERAL_FormatType = 69;
	int LPAREN = 70;
	int RPAREN = 71;
	int PLUS = 72;
	int INTEGER_THING = 73;
	int STAR = 74;
	int LITERAL_I = 75;
	int DOT = 76;
	int HAT = 77;
	int BACKS = 78;
	int SLASH = 79;
	int DOLLAR = 80;
	int SMALER = 81;
	int T1 = 82;
	int T2 = 83;
	int T3 = 84;
	int T4 = 85;
	int T5 = 86;
	int T6 = 87;
	int T7 = 88;
	int T8 = 89;
	int T9 = 90;
	int ID = 91;
	int ID_LETTER = 92;
	int DIGIT = 93;
	int ESC = 94;
	int WS_ = 95;
}
