(ns handotai.murakumo-test
  (:require [clojure.test :refer [deftest is testing]]
            [handotai.murakumo :as handotai]))

(def full-attestations
  (into {}
        (map (fn [gate] [gate (str "attested-" (name gate))]))
        (distinct (mapcat :required-gates (vals handotai/cell-specs)))))

(deftest maps-all-legacy-silicon-cells
  (is (= #{"silicon_cmp"
           "silicon_deposition"
           "silicon_etch"
           "silicon_implant"
           "silicon_litho"
           "silicon_metrology"
           "silicon_packaging"
           "silicon_test"}
         (set (map :legacy-cell (vals handotai/cell-specs))))))

(deftest r0-gates-block-effects
  (let [plan (handotai/cell-plan :litho
                                 {:wafer-lot-id "wafer-001"
                                  :computed-at "2026-06-29T00:00:00Z"})]
    (is (= :blocked (:status plan)))
    (is (= [:council-fleet-attestation
            :r1-activation-adr
            :robot-witness-quorum-baseline
            :silen-force-baseline-review
            :judah-fab-subcluster-baseline
            :duv-euv-design-intent-attestation
            :open-reticle-and-recipe-baseline
            :equipment-dispatch-xrpc-baseline]
           (:missing-gates plan)))
    (is (empty? (:effects plan)))))

(deftest attested-packaging-emits-chip-effect
  (let [plan (handotai/cell-plan :packaging
                                 {:attestations full-attestations
                                  :wafer-lot-id "wafer-001"
                                  :die-id "die-042"
                                  :chip-id "chip-042"
                                  :computed-at "2026-06-29T00:00:00Z"
                                  :record {:tid "pkg-042"
                                           :bondingMode "CoWoS"
                                           :peerIdEfuseBurned true}})
        effect (first (:effects plan))]
    (is (= :ready (:status plan)))
    (is (= :mst/put-record (:op effect)))
    (is (= handotai/actor-did (:actor effect)))
    (is (= "com.etzhayyim.silicon.chipManufacturingAttestation" (:collection effect)))
    (is (= "pkg-042" (:rkey effect)))
    (is (= "CoWoS" (get-in effect [:record :bondingMode])))
    (is (= true (get-in effect [:record :peerIdEfuseBurned])))))

(deftest special-gates-remain-cell-specific
  (testing "implant keeps supplementary high-risk review"
    (let [attestations (dissoc full-attestations :ion-implant-specific-supplementary-review)
          plan (handotai/cell-plan :implant {:attestations attestations})]
      (is (= [:ion-implant-specific-supplementary-review] (:missing-gates plan)))
      (is (empty? (:effects plan)))))
  (testing "metrology keeps no-human-face-data gate"
    (let [attestations (dissoc full-attestations :no-human-face-data-baseline)
          plan (handotai/cell-plan :metrology {:attestations attestations})]
      (is (= [:no-human-face-data-baseline] (:missing-gates plan)))
      (is (empty? (:effects plan))))))

(deftest all-cell-plans-ready-when-attested
  (let [plans (handotai/all-cell-plans {:attestations full-attestations
                                        :wafer-lot-id "wafer-001"
                                        :die-id "die-042"
                                        :chip-id "chip-042"
                                        :computed-at "2026-06-29T00:00:00Z"})]
    (is (= (set (keys handotai/cell-specs)) (set (keys plans))))
    (is (every? #(= :ready (:status %)) (vals plans)))
    (is (= (count handotai/cell-specs)
           (count (mapcat :effects (vals plans)))))))
