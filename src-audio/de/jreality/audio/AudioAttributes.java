package de.jreality.audio;

public interface AudioAttributes {
	public static final String DIRECTIONLESS_PROCESSOR_KEY = "directionlessProcessor";
	public static final String REVERB_TIME_KEY = "reverbTime";
	public static final String SPEED_OF_SOUND_KEY = "speedOfSound";
	public static final String VOLUME_GAIN_KEY = "volumeGain";
	public static final String DIRECTIONLESS_GAIN_KEY = "directionlessVolumeGain";
	public static final String DISTANCE_CUE_KEY = "distanceCue";
	public static final String DIRECTIONLESS_CUE_KEY = "directionlessCue";
	public static final String UPDATE_CUTOFF_KEY = "updateCutoff";
	public static final String FDN_PARAMETER_KEY = "fdnParameters";
	
	public static final float DEFAULT_REVERB_TIME = 1.5f;
	public static final float DEFAULT_GAIN = 1f;
	public static final float DEFAULT_DIRECTIONLESS_GAIN = 0.1f;
	public static final float DEFAULT_SPEED_OF_SOUND = 332f;
	public static final DistanceCue DEFAULT_GENERAL_CUE = new DistanceCue.CONSTANT();
	public static final DistanceCue DEFAULT_DIRECTED_CUE = new DistanceCue.CONSTANT();
	public static final float DEFAULT_UPDATE_CUTOFF = 6f; // play with this parameter if audio gets choppy
	public static final FDNParameters DEFAULT_FDN_PARAMETERS = FDNParameters.BUNNY_PARAMETERS;
	
	public static final float HEARING_THRESHOLD = 1e-16f; // dynamic range between hearing threshold and instant perforation of eardrum
}