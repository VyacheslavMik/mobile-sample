(ns mobile-sample.lib.rn
  (:require [reagent.core :as r]))

;; React-Native
(def react-native (js/require "react-native"))
(def app-registry (.-AppRegistry react-native))
(def keyboard (.-Keyboard react-native))
(def keyboard-avoiding-view (r/adapt-react-class (.-KeyboardAvoidingView react-native)))
(def dimensions (.-Dimensions react-native))
(def app-state (.-AppState react-native))
(def window (js->clj (.get dimensions "window") :keywordize-keys true))
(def platform (js->clj (.-Platform react-native) :keywordize-keys true))
(def async-storage (.-AsyncStorage react-native))
(def alert (.-Alert react-native))
(def image (r/adapt-react-class (.-Image react-native)))
(def scroll-view (r/adapt-react-class (.-ScrollView react-native)))
(def web-view (r/adapt-react-class (.-WebView react-native)))
(def linking (.-Linking react-native))
(def touchable-opacity (r/adapt-react-class (.-TouchableOpacity react-native)))
(def touchable-without-feedback (r/adapt-react-class (.-TouchableWithoutFeedback react-native)))
(def modal (r/adapt-react-class (.-Modal react-native)))
(def is-ios (= "ios" (:OS platform)))
(def text (r/adapt-react-class (.-Text react-native)))
(def view (r/adapt-react-class (.-View react-native)))
(def activity-indicator (r/adapt-react-class (.-ActivityIndicator react-native)))
(def text-input (r/adapt-react-class (.-TextInput react-native)))

(defn register [nm cmp]
  (.registerComponent app-registry nm #(r/reactify-component cmp)))
