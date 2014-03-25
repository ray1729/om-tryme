(ns om-tryme.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [secretary.core :as secretary :include-macros true :refer [defroute]]
            [goog.events :as events])
  (:import goog.History
           goog.history.EventType))

(enable-console-print!)

(defn- gen-item
  [n]
  {:id n :status (rand-nth [:created :pending :done])})

(def initial-state {:page :dashboard
                    :params nil
                    :items (mapv gen-item (range 50))})

(def app-state (atom initial-state))

(defn init-process
  [app status]
  (let [work (vec (filter #(= (:status %) status) (:items app)))]
    (assoc app
      :page :process
      :params {:work work :nitems (count work) :pos 0})))

(defn init-edit
  [app id]
  (if-let [item (get-in app [:items id] nil)]
    (assoc app :page :edit :params {:item item})
    app))

;;
;; Routing
;;

(secretary/set-config! :prefix "#")

(defroute dashboard-path "/" []
  (swap! app-state assoc :page :dashboard))

(defroute process-path "/process/:status" [status]
  (swap! app-state init-process (keyword status)))

(defroute edit-path "/edit/:id" [id]
  (swap! app-state init-edit id))

(let [h (History.)]
  (goog.events/listen h EventType.NAVIGATE #(secretary/dispatch! (.-token %)))
  (.setEnabled h true))

;;
;; Dashboard component
;;

(defn dashboard
  [app owner]
  (reify
    om/IRender
    (render
     [_]
     (let [count-for (frequencies (map :status (:items app)))]
       (dom/table nil
                  (apply dom/tbody nil
                       (map (fn [status]
                              (dom/tr nil
                                      (dom/td nil (dom/a #js {:href (process-path {:status (name status)})} (name status))
                                      (dom/td nil (count-for status)))))
                            [:created :pending :done])))))))

;;
;; Pager
;;

(defn pager
  [{:keys [pos nitems] :as params} owner]
  (reify
    om/IRender
    (render
     [_]
     (println "page" (inc pos) "of" nitems)
     (let [prev-pos (dec pos)
           next-pos (inc pos)]
       (dom/div nil
                (when (> prev-pos 0)
                  (dom/button #js {:onClick (fn [_] (om/transact! params [:pos] dec))} "Prev"))
                (when (< next-pos nitems)
                  (dom/button #js {:onClick (fn [_] (om/transact! params [:pos] inc))} "Next")))))))

;;
;; Edit component
;;

(defn edit
  [{:keys [item]} owner]
  (reify
    om/IRender
    (render
     [_]
     (dom/div nil
              (dom/h1 nil "Edit")
              (dom/pre nil (pr-str item))
              (dom/a #js {:href (dashboard-path)} "Home")))))

;;
;; Process component
;;

(defn process
  [{:keys [work pos nitems] :as params} owner]
  (reify
    om/IRender
    (render
     [_]
     (dom/div nil
              (om/build edit {:item (work pos)})
              (om/build pager params)))))

;;
;; Main component
;;

(defn main
  [app owner]
  (reify
    om/IRender
    (render [_]
            (case (:page app)
              :dashboard (om/build dashboard app)
              :edit      (om/build edit (:params app))
              :process   (om/build process (:params app))))))

(om/root main app-state
  {:target (. js/document (getElementById "app"))})
