
from flask import Flask, request, jsonify
from keras.models import load_model
from keras.preprocessing import image
import numpy as np
import os

app = Flask(__name__)

# 모델 로드
model_path = 'model/newbuilding_model.h5'
model = load_model(model_path)

class_mapping = {'0': '1', '1': '6', '2': '7'}

# 이미지 저장 디렉토리
UPLOAD_FOLDER = 'uploads'
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

# 이미지 전처리 함수
def preprocess_image(img):
    img = img.resize((150, 150))  # 이미지 크기 조정
    img_array = image.img_to_array(img)
    img_array = np.expand_dims(img_array, axis=0)
    img_array /= 255.0
    return img_array

@app.route('/predict', methods=['POST'])
def predict():
    if 'file' not in request.files:
        return jsonify({'error': 'No file part'})

    file = request.files['file']

    if file.filename == '':
        return jsonify({'error': 'No selected file'})

    if file:
        # 이미지 저장
        filename = os.path.join(UPLOAD_FOLDER, file.filename)
        file.save(filename)

        # 이미지 전처리
        img = image.load_img(filename)
        img_array = preprocess_image(img)

        # 모델 예측
        result = model.predict(img_array)

        # 예측 결과 및 확률을 JSON 형태로 반환
        response_data = {
            'predicted_class': class_mapping[str(np.argmax(result))],
            'class_probabilities': {
                class_mapping['0']: float(result[0][0]),
                class_mapping['1']: float(result[0][1]),
                class_mapping['2']: float(result[0][2])
            }
        }

        return jsonify(response_data)

@app.route('/upload', methods=['GET','POST'])
def upload():
    test_image_path = ('test/1_2.jpg')
    img = image.load_img(test_image_path)
    img_array = preprocess_image(img)
    result = model.predict(img_array)

    response_data = {
        'predicted_class': class_mapping[str(np.argmax(result))],
        'class_probabilities': {
            class_mapping['0']: float(result[0][0]),
            class_mapping['1']: float(result[0][1]),
            class_mapping['2']: float(result[0][2])
        }
    }

    return jsonify(response_data)

if __name__ == '__main__':
    app.run(debug=True, port='5000', host='180.226.49.210')

    


