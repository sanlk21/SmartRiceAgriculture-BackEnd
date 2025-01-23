#!/usr/bin/env python

import sys
import json
import logging

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)

_missing_deps = []
try:
    import numpy as _np
except ImportError:
    _missing_deps.append('numpy')
try:
    import tensorflow as _tf
except ImportError:
    _missing_deps.append('tensorflow')
try:
    from pathlib import Path as _Path
except ImportError:
    _missing_deps.append('pathlib')
try:
    from sklearn.preprocessing import StandardScaler as _StandardScaler
except ImportError:
    _missing_deps.append('scikit-learn')
try:
    import joblib as _joblib
except ImportError:
    _missing_deps.append('joblib')

if _missing_deps:
    logging.error(f"Missing required packages: {', '.join(_missing_deps)}")
    logging.error("Please install required packages using:")
    logging.error("pip install -r requirements.txt")
    sys.exit(1)

np, tf, Path, StandardScaler, joblib = _np, _tf, _Path, _StandardScaler, _joblib

def load_ml_resources():
    try:
        script_dir = Path(__file__).parent
        model = tf.keras.models.load_model(str(script_dir / 'weather_prediction_model.keras'))
        scaler = joblib.load(str(script_dir / 'weather_scalers.pkl'))
        return model, scaler
    except Exception as e:
        logging.error(f"Error loading ML resources: {str(e)}")
        sys.exit(1)

def predict_weather(input_data):
    try:
        model, scaler = load_ml_resources()
        input_array = np.array(json.loads(input_data))

        if input_array.ndim == 1:
            input_array = input_array.reshape(1, -1)

        scaled_data = scaler.transform(input_array)
        prediction = model.predict(scaled_data, verbose=0)
        result = scaler.inverse_transform(prediction)[0]
        print(json.dumps(result.tolist()))

    except Exception as e:
        logging.error(f"Error making prediction: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) != 2:
        logging.error("Usage: python weather_predict.py '[input_data_array]'")
        sys.exit(1)

    predict_weather(sys.argv[1])