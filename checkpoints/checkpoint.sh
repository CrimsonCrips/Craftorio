#!/usr/bin/env bash
set -euo pipefail

root="$(git -C "$(dirname "$0")" rev-parse --show-toplevel)"
cd "$root"

dir="checkpoints"
snapshot="$dir/.snapshot"

last="$(ls "$dir" | grep -E '^v[0-9]+\.patch$' | sed -E 's/^v([0-9]+)\.patch$/\1/' | sort -n | tail -1 || true)"
version=$(( ${last:-0} + 1 ))

work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT
mkdir -p "$work/a" "$work/b"

tracked() {
    git ls-files -co --exclude-standard \
        | grep -v -e "^$dir/" -e '^src/generated/resources/\.cache/' || true
}

tracked | while IFS= read -r file; do
    [ -f "$file" ] || continue
    mkdir -p "$work/b/$(dirname "$file")"
    cp -p "$file" "$work/b/$file"
done

if [ -d "$snapshot" ]; then
    base="checkpoint v$last"
    cp -a "$snapshot/." "$work/a/"
else
    base="last commit $(git rev-parse --short HEAD)"
    git archive HEAD | tar -x -C "$work/a"
    rm -rf "$work/a/$dir" "$work/a/src/generated/resources/.cache"
fi

(cd "$work" && git diff --no-index --no-prefix a b > patch || true)
(cd "$work" && git diff --no-index --no-prefix --name-status a b > files || true)

mv "$work/patch" "$dir/v$version.patch"

rm -rf "$snapshot"
mv "$work/b" "$snapshot"

echo "version: v$version"
echo "date: $(date '+%Y-%m-%d %H:%M')"
echo "compared against: $base"
echo "patch: $dir/v$version.patch"
echo "changed files:"
sed -E 's#\t[ab]/#\t#g' "$work/files"
