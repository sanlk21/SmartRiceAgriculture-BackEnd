import sys
import json
import numpy as np
from pathlib import Path
import pandas as pd
from tensorflow.keras.models import load_model
from sklearn.preprocessing import StandardScaler
import joblib
from datetime import datetime, timedelta

def load_ml_resources():
    try:
        script_dir = Path(__file__).parent
        model = load_model(script_dir / 'weather_prediction_model.keras')
        scaler = joblib.load(script_dir / 'weather_scalers.pkl')
        return model, scaler
    except Exception as e:
        print(f"Error loading ML resources: {str(e)}", file=sys.stderr)
        sys.exit(1)

def generate_sample_weather_data():
    """Generate sample weather data for testing"""
    sample_data = []
    current_date = datetime.now()

    for i in range(7):  # Generate 7 days of data
        date = current_date + timedelta(days=i)
        sample_data.append({
            'date': date.strftime('%Y-%m-%d'),
            'temperature_2m_max': round(np.random.uniform(25, 35), 2),
            'temperature_2m_min': round(np.random.uniform(20, 25), 2),
            'precipitation_sum': round(np.random.uniform(0, 50), 2),
            'wind_speed_10m_max': round(np.random.uniform(5, 15), 2),
            'wind_gusts_10m_max': round(np.random.uniform(10, 25), 2),
            'shortwave_radiation_sum': round(np.random.uniform(10, 20), 2)
        })
    return sample_data

def process_weather_data(data):
    """Process weather data to create model inputs"""
    df = pd.DataFrame(data)
    features = [
        'temperature_2m_max', 'temperature_2m_min',
        'precipitation_sum', 'wind_speed_10m_max',
        'wind_gusts_10m_max', 'shortwave_radiation_sum'
    ]
    return df[features].values

def get_weather_type(temp, rain_prob):
    if rain_prob > 0.7:
        return "HEAVY_RAIN"
    elif rain_prob > 0.3:
        return "LIGHT_RAIN"
    elif temp > 32:
        return "HOT"
    elif temp < 22:
        return "COOL"
    else:
        return "FAIR"

def predict_weather(input_data=None):
    try:
        # If no input data provided, generate sample data
        if input_data is None:
            input_data = generate_sample_weather_data()
            print("Using generated sample data:", json.dumps(input_data, indent=2))
        else:
            input_data = json.loads(input_data)

        # Load model and scaler
        model, scaler = load_ml_resources()

        # Process input data
        processed_data = process_weather_data(input_data)

        # Scale the data
        scaled_data = scaler.transform(processed_data)

        # Make predictions
        predictions = model.predict(scaled_data, verbose=1)

        # Format predictions with dates
        results = []
        for i, pred in enumerate(predictions):
            # Calculate rainfall probability based on precipitation and temperature
            max_temp = input_data[i]['temperature_2m_max']
            precip = input_data[i]['precipitation_sum']
            rain_prob = min(precip / 50.0, 1.0)  # Normalize precipitation to probability

            temp = float(pred[0])
            # Ensure temperature is within reasonable bounds
            temp = max(min(temp, 40), 15)  # Clamp between 15°C and 40°C

            result = {
                'date': input_data[i]['date'],
                'temperature': round(temp, 1),
                'rainfall_probability': round(rain_prob, 2),
                'wind_speed': round(float(input_data[i]['wind_speed_10m_max']), 1),
                'weather_type': get_weather_type(temp, rain_prob)
            }
            results.append(result)

        print(json.dumps(results, indent=2))
        return results

    except Exception as e:
        print(f"Error making prediction: {str(e)}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) == 1:
        # No arguments provided, use sample data
        predict_weather()
    elif len(sys.argv) == 2:
        # Weather data JSON provided as argument
        predict_weather(sys.argv[1])
    else:
        print("Usage: python weather_predict.py '[weather_data_json]'", file=sys.stderr)
        sys.exit(1)