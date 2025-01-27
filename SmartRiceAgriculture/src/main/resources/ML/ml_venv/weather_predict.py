import sys
import json
import numpy as np
from pathlib import Path
import pandas as pd
from tensorflow.keras.models import load_model
from sklearn.preprocessing import StandardScaler
import joblib

def load_ml_resources():
    try:
        # Get the directory containing this script
        script_dir = Path(__file__).parent

        # Load model and scalers from the same directory
        model = load_model(script_dir / 'weather_prediction_model.h5')
        scaler = joblib.load(script_dir / 'weather_scalers.pkl')
        return model, scaler
    except Exception as e:
        print(f"Error loading ML resources: {str(e)}", file=sys.stderr)
        sys.exit(1)

def process_weather_data(data):
    """Process weather data to create model inputs"""
    df = pd.DataFrame(data)
    features = ['temperature_2m_max', 'temperature_2m_min', 'precipitation_sum',
                'wind_speed_10m_max', 'wind_gusts_10m_max', 'shortwave_radiation_sum']
    return df[features].values

def predict_weather(input_data):
    try:
        # Load model and scaler
        model, scaler = load_ml_resources()

        # Process input data
        processed_data = process_weather_data(input_data)

        # Scale the data
        scaled_data = scaler.transform(processed_data)

        # Make prediction
        predictions = model.predict(scaled_data)

        # Format predictions
        results = []
        for i, pred in enumerate(predictions):
            results.append({
                'temperature': float(pred[0]),
                'rainfall_probability': float(pred[1]),
                'humidity': float(pred[2])
            })

        return results

    except Exception as e:
        print(f"Error making prediction: {str(e)}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python weather_predict.py '[weather_data_json]'", file=sys.stderr)
        sys.exit(1)

    try:
        # Parse input JSON
        input_data = json.loads(sys.argv[1])
        predictions = predict_weather(input_data)
        print(json.dumps(predictions))
    except Exception as e:
        print(f"Error: {str(e)}", file=sys.stderr)
        sys.exit(1)