let
  nixpkgs-version = "20.03";
  pkgs = import (builtins.fetchTarball {
    name = "nixpkgs-${nixpkgs-version}";
    url = "https://github.com/nixos/nixpkgs/archive/${nixpkgs-version}.tar.gz";
    sha256 = "0182ys095dfx02vl2a20j1hz92dx3mfgz2a6fhn31bqlp1wa8hlq";
  }) {};
  jekyll = pkgs.writeScriptBin "jekyll" ''
    ${pkgs.bundler}/bin/bundle && ${pkgs.bundler}/bin/bundle exec jekyll "$@"
  '';
in pkgs.stdenv.mkDerivation {
  name = "case-insensitive";
  buildInputs = [
    jekyll
    pkgs.git
    pkgs.sbt
  ];
}
