import joblib
import numpy as np
from tensorflow.keras.models import load_model

# Load model and scalers
model = load_model('weather_prediction_model.keras')
scalers = joblib.load('weather_scalers.pkl')

def predict_weather(input_data):
    # Convert input data to NumPy array if not already
    if not isinstance(input_data, np.ndarray):
        input_data = np.array(input_data)
    
    # Scale input data
    scaled_data = scalers.transform(input_data.reshape(1, -1))
    
    # Make prediction
    prediction = model.predict(scaled_data)
    
    # Inverse transform prediction
    return scalers.inverse_transform(prediction)[0]

if __name__ == "__main__":
    # Example input for local testing
    sample_input = [30.0, 75.0, 15.0]  # Replace with your model's input format
    print(predict_weather(sample_input))
