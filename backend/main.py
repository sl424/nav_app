# [START app]
import logging

from flask import Flask, jsonify, request
from flask_restful import reqparse, abort, Api, Resource
import json
import cgi
import urllib
import re

import flask_cors
from google.appengine.ext import ndb
from google.appengine.ext.ndb import metadata

import google.auth.transport.requests
import google.oauth2.id_token
import requests_toolbelt.adapters.appengine

# Use the App Engine Requests adapter. This makes sure that Requests uses
# URLFetch.
requests_toolbelt.adapters.appengine.monkeypatch()
HTTP_REQUEST = google.auth.transport.requests.Request()

app = Flask(__name__)
flask_cors.CORS(app)
api = Api(app)

# [START note]
class Note(ndb.Expando):
    """NDB model class for a user's note.
    Key is user id from decrypted token.
    """
    friendly_id = ndb.StringProperty()
    message = ndb.TextProperty()
    address = ndb.TextProperty()
    latitude = ndb.TextProperty()
    longitude = ndb.TextProperty()
    created = ndb.DateTimeProperty(auto_now_add=True)
# [END note]


# [START query_database]
def query_database(user_id):
    """Fetches all notes associated with user_id.
    Notes are ordered them by date created, with most recent note added
    first.
    """
    ancestor_key = ndb.Key(Note, user_id)
    query = Note.query(ancestor=ancestor_key).order(-Note.created)
    #query = Note.query()

    notes = query.fetch()

    note_messages = []

    for note in notes:
        obj = note.to_dict()
        obj['id'] = (note.key).urlsafe()
        note_messages.append(obj)
    return note_messages

# [END query_database]

def abort_if_failed(note_id):

    id_token = request.headers['Authorization'].split(' ').pop()
    claims = google.oauth2.id_token.verify_firebase_token(
        id_token, HTTP_REQUEST)
    if not claims:
        abort(401, message="Unauthorized")
        
    key = ndb.Key(urlsafe=note_id)
    #if not key:
        #abort(404, message="note {} doesn't exist".format(note_id))

    #ancestor_key = ndb.Key(Note, claims['sub'])
    note = key.get()
    #k,i = note.key.flat()
    if claims['sub'] not in note.key.flat():
        abort(401, message="note {} Unauthorized".format(note_id))

    return note

class anote(Resource):
    def get(self, note_id):
        note = abort_if_failed(note_id)
        #key = ndb.Key(urlsafe=note_id)
        #note = key.get()
        """
        obj = []
        tmp = note.to_dict()
        tmp['id'] = (note.key).urlsafe()
        obj.append(tmp)
        obj.append({ 
            'friendly_id': note.friendly_id,
            'message': note.message,
            'address': note.address,
            'id': (note.key).urlsafe()
           })
        """
        #return jsonify(friendly_id= note.friendly_id, message= note.message, address= note.address, id= (note.key).urlsafe() )
        return jsonify({
            'friendly_id': note.friendly_id,
            'message': note.message,
            'address': note.address,
            'latitude': note.latitude,
            'longitude': note.longitude,
            'created': note.created,
            'id': (note.key).urlsafe()
            })

    def delete(self, note_id):
        note = abort_if_failed(note_id)
        note.key.delete()
        return '', 204

    def patch(self, note_id):
        note = abort_if_failed(note_id)
        # retrieve data and properties
        data = request.get_json()
        tlist = note._properties                                                

        for name,value in data.items():                                        
            if type(value) == type([]):                                        
                ls = getattr(note, name)                                        
                for v in value:                                                
                    if v in ls:                                                
                        ls.remove(v)                                           
                    else:                                                      
                        ls.append(v)                                           
                setattr(note, name, ls)                                         
            else:                                                              
                setattr(note, name, value)                                      
        note.put()
        return 'PATCHED', 202

def checkAuth():
    id_token = request.headers['Authorization'].split(' ').pop()
    claims = google.oauth2.id_token.verify_firebase_token(
            id_token, HTTP_REQUEST)
    if not claims:
        abort(401, message="Unauthorized")
        #return 'Unauthorized', 401
    return claims

class list_note(Resource):

    def get(self):
        claims = checkAuth()
        notes = query_database(claims['sub'])
        #notes = query_database("note")
        return jsonify(notes)

    def delete(self):
        claims = checkAuth()
        ancestor_key = ndb.Key(Note, claims['sub'])
        query = Note.query(ancestor=ancestor_key).order(-Note.created)
        notes = query.fetch()
        nkeylist = [note.key for note in notes]
        ndb.delete_multi(nkeylist)
        return "DELETED ALL", 204

    def post(self):
        claims = checkAuth()
        data = request.get_json()
        note = Note(
            parent=ndb.Key(Note, claims['sub']),
            message=data['message'],
            latitude=data['latitude'],
            longitude=data['longitude'],
            address=data['address'])
        note.friendly_id = claims.get('name', claims.get('email', 'Unknown'))
        note.put()
        return 'OK', 200

api.add_resource(anote, '/notes/<note_id>')
api.add_resource(list_note, '/notes')

if __name__ == '__main__':
    app.run(debug=True)
