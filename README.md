# com-etzhayyim-handotai

`handotai` is the Etzhayyim domain actor for semiconductor fabrication and
semiconductor intelligence surfaces.

## Migration Boundary

`kotoba-lang/kotodama-cells/silicon_*` is legacy source
during migration. New silicon fabrication cell orchestration belongs in
`src/handotai/murakumo.cljc` as pure `.cljc` actor plans. The plans preserve R0
scaffold behavior by emitting no write effects until Council fleet, silen-force,
equipment placement, process safety, AI dataset, and R-phase attestations are
supplied. Host placement remains in `kotoba-lang/murakumo`; AT Protocol/PDS
surfaces remain in `gftdcojp/app-aozora`.

The existing app/intelligence manifest files in this repo remain separate from
the domain-cell actor boundary. The actor owner is `handotai`, while the fab
record collections intentionally preserve the legacy `com.etzhayyim.silicon.*`
surface used by the old `silicon_*` cells.

## Legacy Cell Coverage

- `silicon_litho`
- `silicon_deposition`
- `silicon_etch`
- `silicon_implant`
- `silicon_cmp`
- `silicon_metrology`
- `silicon_test`
- `silicon_packaging`

Run the focused actor checks with:

```sh
bb test
```
