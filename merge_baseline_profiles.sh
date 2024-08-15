#!/bin/bash

# Ścieżka do katalogu z plikami baseline-prof.txt
input_dir="benchmarks/build/intermediates/managed_device_android_test_additional_output/demoNonMinifiedRelease/pixel6Api33DemoNonMinifiedReleaseAndroidTest"
# Ścieżka do wynikowego pliku
output_file="app/src/main/baseline-prof.txt"

# Zmienna do śledzenia liczby znalezionych plików
file_count=0

echo "Szukam plików *baseline-prof.txt i *startup-prof.txt w katalogu $input_dir..."

# Iteracja przez wszystkie pliki kończące się na baseline-prof.txt lub startup-prof.txt w katalogu wejściowym
for file in $(find "$input_dir" -type f \( -name "*baseline-prof.txt" -o -name "*startup-prof.txt" \)); do
  if [ -f "$file" ]; then
    echo "Znaleziono plik: $file"
    # Dodanie zawartości pliku do wynikowego z wymuszeniem nowej linii na końcu każdej linii
    while IFS= read -r line; do
      echo "$line" >> "$output_file"
    done < "$file"
    file_count=$((file_count + 1))
  fi
done

if [ $file_count -eq 0 ]; then
  echo "Nie znaleziono żadnych plików kończących się na baseline-prof.txt ani startup-prof.txt w katalogu $input_dir"
else
  # Usuwanie duplikatów i sortowanie
  sort -u "$output_file" -o "$output_file"
  echo "Plik baseline-prof.txt został pomyślnie utworzony w $output_file z $file_count plików źródłowych"
fi
