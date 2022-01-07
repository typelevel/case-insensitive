{
  description = "Provision a dev environment";

  inputs = {
    typelevel-nix.url = "github:rossabaker/typelevel-nix";
    nixpkgs.follows = "typelevel-nix/nixpkgs";
    flake-utils.follows = "typelevel-nix/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils, typelevel-nix }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
        jekyll = pkgs.writeScriptBin "jekyll" ''
          ${pkgs.bundler}/bin/bundle && ${pkgs.bundler}/bin/bundle exec jekyll "$@"
        '';
      in
      {
        devShell = typelevel-nix.devShells.${system}.library.overrideAttrs (old: {
          buildInputs = old.buildInputs ++ [ jekyll ];
        });
      }
    );
}
