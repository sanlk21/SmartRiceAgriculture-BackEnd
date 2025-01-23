#!/usr/bin/env python
import sys
import json
import os
import numpy as np
from pathlib import Path
from tensorflow.keras.models import load_model
import joblib
from pathlib import Path


def load_ml_resources():
    try:
        # Get model directory from environment variable
        model_dir = os.getenv('MODEL_DIR', 'ml')
        model_dir = Path(model_dir)

        # Load model and scalers
        model = load_model(model_dir / 'weather_prediction_model.keras')
        scalers = joblib.load(model_dir / 'weather_scalers.pkl')
        script_dir = Path(__file__).parent
        model = load_model(script_dir / 'weather_prediction_model.keras')
        scalers = joblib.load(script_dir / 'weather_scalers.pkl')
        return model, scalers
    except Exception as e:
        print(f"Error loading ML resources: {str(e)}", file=sys.stderr)
        logging.error(f"Error loading ML resources: {str(e)}")
        sys.exit(1)

def predict_weather(input_data):
    try:
        model, scalers = load_ml_resources()

        # Convert input to numpy array
        input_array = np.array(json.loads(input_data))

        # Scale input data
        scaled_data = scalers.transform(input_array.reshape(1, -1))

        # Make prediction
        prediction = model.predict(scaled_data, verbose=0)

        # Inverse transform and return
        result = scalers.inverse_transform(prediction)[0]
        print(json.dumps(result.tolist()))

    except Exception as e:
        print(f"Error making prediction: {str(e)}", file=sys.stderr)
        logging.error(f"Error making prediction: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python weather_predict.py '[input_data_array]'", file=sys.stderr)
        logging.error("Usage: python weather_predict.py '[input_data_array]'")
        sys.exit(1)

    predict_weather(sys.argv[1])