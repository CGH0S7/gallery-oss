# Edge Gallery OSS

Fork of Google AI Edge Gallery focused on a cleaner offline Android experience.

This fork removes the dependency on Firebase/GMS at runtime, removes online model recommendation and download flows, and turns the app into a local-model-first workflow for devices and regions where the upstream experience is not ideal.

## What Changed

- Runtime behavior is local-first: only locally prepared model files are supported.
- Online model recommendation and in-app download have been removed from the main workflow.
- A more prominent local model import entry is available in the model screens.
- Imported models are shown with a cleaner display name instead of the raw file name.
- App branding has been updated to `Edge Gallery OSS`.

## Supported Model Import

Current import flow is designed for local LiteRT-LM model files:

- Supported formats: `.litertlm`, `.task`
- Typical target ABI build: `arm64-v8a`

Import flow:

1. Prepare the model file on your phone.
2. Open the target capability inside the app.
3. Tap `Import local model`.
4. Import the file and start chatting locally.

## Build Notes

Android build files are under `Android/src`.

This fork is configured to generate only `arm64-v8a` split APKs by default to reduce package size.

If you build from Android Studio:

1. Open `Android/src` as the project.
2. Sync Gradle.
3. Build the `release` or `debug` variant.

## Scope

This repository is intended as a practical OSS fork for:

- phones without stable GMS availability
- networks where Hugging Face access is unreliable
- users who prefer fully local model management

## License

Licensed under Apache 2.0. See [LICENSE](LICENSE).
