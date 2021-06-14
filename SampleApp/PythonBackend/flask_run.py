from flask import Flask,request
import werkzeug
app = Flask(__name__)

@app.route('/', methods=['GET', 'POST'])
def handle_request():
	imagefile = request.files['image']
	filename = werkzeug.utils.secure_filename(imagefile.filename)
	print("Recieved file: "+ imagefile.filename)
	return "image uploaded successfully"
app.run(host="0.0.0.0", port=5000, debug=True)


