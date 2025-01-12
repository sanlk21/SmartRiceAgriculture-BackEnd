import os
import numpy as np
from sklearn.preprocessing import StandardScaler
import tensorflow as tf
import joblib
from pathlib import Path

# Disable GPU
os.environ['CUDA_VISIBLE_DEVICES'] = '-1'

def verify_versions():
    print("Checking installed versions:")
    print(f"NumPy: {np.__version__}")
    print(f"TensorFlow: {tf.__version__}")
    import scipy
    print(f"SciPy: {scipy.__version__}")
    import sklearn
    print(f"Scikit-learn: {sklearn.__version__}")
    print(f"Joblib: {joblib.__version__}")
    print("\nAll imports successful!")

def create_test_model():
    print("\nStarting model creation...")

    # Create directory - fixing the path to be in the correct location
    current_dir = Path(__file__).parent
    ml_dir = current_dir
    print(f"Using directory: {ml_dir.absolute()}")

    # Create simple model
    with tf.device('/CPU:0'):
        model = tf.keras.Sequential([
            tf.keras.layers.Dense(10, activation='relu', input_shape=(3,)),
            tf.keras.layers.Dense(3)
        ])
        model.compile(optimizer='adam', loss='mse')
    print("Model created")

    # Create scaler
    scaler = StandardScaler()
    dummy_data = np.random.rand(100, 3)
    scaler.fit(dummy_data)
    print("Scaler created")

    # Save files
    model_path = ml_dir / 'weather_prediction_model.keras'
    scaler_path = ml_dir / 'weather_scalers.pkl'

    model.save(model_path)
    joblib.dump(scaler, scaler_path)

    print("\nFiles saved to:")
    print(f"Model: {model_path.absolute()}")
    print(f"Scaler: {scaler_path.absolute()}")

    # Verify files exist
    print("\nVerifying files:")
    print(f"Model file exists: {model_path.exists()}")
    print(f"Scaler file exists: {scaler_path.exists()}")

if __name__ == "__main__":
    try:
        verify_versions()
        create_test_model()
        print("\nSuccess! All operations completed.")
    except Exception as e:
        print(f"\nError occurred: {str(e)}")
        raise