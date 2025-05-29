from flask import Flask, request, jsonify, send_from_directory, abort
import os
import subprocess

app = Flask(__name__)

CONFIGS_DIR = "/etc/openvpn/clients"
CCD_DIR = "/etc/openvpn/ccd"
INSTALL_SCRIPT = "/root/openvpn-install.sh"
API_TOKEN = "alibaba-4vps-turk"

def check_auth(req):
    return req.headers.get("Authorization") == f"Bearer {API_TOKEN}"

@app.route("/api/configs/<client_name>", methods=["GET"])
def get_config(client_name):
    if not check_auth(request):
        return abort(403, "Unauthorized")

    filename = f"{client_name}.ovpn"
    path = os.path.join(CONFIGS_DIR, filename)
    if not os.path.isfile(path):
        return abort(404, "Config not found")

    return send_from_directory(CONFIGS_DIR, filename, as_attachment=True)

@app.route("/api/block/<client_name>", methods=["POST"])
def block_client(client_name):
    if not check_auth(request):
        return abort(403, "Unauthorized")

    ccd_path = os.path.join(CCD_DIR, client_name)
    try:
        with open(ccd_path, "w") as f:
            f.write("disable\n")
        return jsonify({"status": "blocked"})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/api/create/<client_name>", methods=["POST"])
def create_config(client_name):
    if not check_auth(request):
        return abort(403, "Unauthorized")

    try:
        subprocess.run(["sudo", INSTALL_SCRIPT, "--new-client", client_name], check=True)
        return jsonify({"status": "created"})
    except subprocess.CalledProcessError as e:
        return jsonify({"error": f"Install script failed: {e}"}), 500

@app.route("/api/active-users", methods=["GET"])
def active_users():
    if not check_auth(request):
        return abort(403, "Unauthorized")

    try:
        cmd = "cat /var/log/openvpn/status.log | awk '/Common Name/{flag=1;next}/ROUTING TABLE/{exit}flag' | wc -l"
        output = subprocess.check_output(cmd, shell=True).decode().strip()
        return jsonify({"active_users": int(output)})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/api/block/<client_name>", methods=["DELETE"])
def delete_client_block(client_name):
    if not check_auth(request):
        return abort(403, "Unauthorized")

    ccd_path = os.path.join(CCD_DIR, client_name)

    try:
        if os.path.exists(ccd_path):
            os.remove(ccd_path)
            return jsonify({"status": "unblocked"})
        else:
            return jsonify({"status": "not_found", "message": "No block file for client"}), 404
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=9898)
